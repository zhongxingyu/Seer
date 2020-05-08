 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.clinicalsummary.web.controller;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Cohort;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.clinicalsummary.SummaryConstants;
 import org.openmrs.module.clinicalsummary.engine.GeneratorThread;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.pdf.PdfCopy;
 import com.lowagie.text.pdf.PdfReader;
 
 /**
  */
 @Controller
 public class GenerateSummaryFormController {
 	
 	private static final Log log = LogFactory.getLog(GenerateSummaryFormController.class);
 	
 	private static final int BUFFER_SIZE = 4096;
 	
 	private static final String GENERATOR_DELAY = "clinicalsummary.generator.delay";
 	
 	private static final String PDF_EXTENSION = ".pdf";
 	
 	private static final String PDF_MIME_TYPE = "application/pdf";
 	
 	@RequestMapping("/module/clinicalsummary/generate")
 	public String generate(final @RequestParam(required = true, value = "patientId") int patientId,
 	                       final HttpServletRequest request, final HttpServletResponse response) {
 		if (Context.isAuthenticated()) {
 			
 			Cohort cohort = new Cohort();
 			cohort.addMember(patientId);
 			
 			String delayString = Context.getAdministrationService().getGlobalProperty(GENERATOR_DELAY);
 			Integer delay = NumberUtils.toInt(delayString, 3000);
 			
 			// if someone specify a delay, then that means we are given a time window to generate the summary sheet
 			if (delay > 0)
 				try {
 					ExecutorService executorService = Executors.newFixedThreadPool(1);
 					GeneratorThread generatorThread = new GeneratorThread(cohort);
 					executorService.execute(generatorThread);
 					if (executorService.awaitTermination(delay, TimeUnit.MILLISECONDS))
 						prepareAttachment(response, patientId);
 				} catch (Exception e) {
 					String referer = request.getHeader("Referer");
 					return "redirect:" + referer;
 				}
 			else
 				// we don't have any delay, so we just return the attachment
 				prepareAttachment(response, patientId);
 		}
 		
 		return StringUtils.EMPTY;
 	}
 	
 	private void prepareAttachment(HttpServletResponse response, Integer patientId) {
 		try {
 			//Prepare response
 			// create a temporary file that will hold all copied pdf file
 			File summaryCollectionsFile = File.createTempFile("Summary", PDF_EXTENSION);
 			summaryCollectionsFile.deleteOnExit();
 			
 			FileOutputStream outputStream = new FileOutputStream(summaryCollectionsFile);
 			
 			Document document = new Document();
 			PdfCopy copy = new PdfCopy(document, outputStream);
 			document.open();
 			
 			Calendar calendar = Calendar.getInstance();
 			
 			PdfReader reader = null;
 			
 			// use file filter to get the patient's files and then iterate.
 			// instead of using the applicable templates
 			
 			File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
 			FileFilter fileFilter = new WildcardFileFilter(patientId + "_*.pdf");
 			
 			for (File summaryFile : folder.listFiles(fileFilter)) {
 				// if the patient is not generated yet, then just skip ...
 				if (!summaryFile.exists())
 					continue;
 				
 				calendar.setTimeInMillis(summaryFile.lastModified());
 				
 				try {
 					// when one pdf fail, then we just need to skip that file
 					// instead of failing for the whole pdfs collection
 					reader = new PdfReader(summaryFile.getAbsolutePath());
 					int pageCount = reader.getNumberOfPages();
 					for (int i = 1; i <= pageCount; i++)
 						copy.addPage(copy.getImportedPage(reader, i));
 				} catch (Exception e) {
 					log.error("Failed to add summary for patient: " + patientId, e);
 				}
 			}
 			
 			document.close();
 			outputStream.close();
 			
 			String time = new SimpleDateFormat("yyyyMMdd_HHmmss_").format(calendar.getTime());
 			String downloadFilename = "PreGeneratedSummary_" + time + patientId + PDF_EXTENSION;
 			
 			response.setHeader("Content-Disposition", "attachment; filename=" + downloadFilename);
 			response.setContentType(PDF_MIME_TYPE);
 			response.setContentLength((int) summaryCollectionsFile.length());
 			response.flushBuffer();
 			
 			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(summaryCollectionsFile));
 			
 			ReadableByteChannel input = Channels.newChannel(inputStream);
 			WritableByteChannel output = Channels.newChannel(response.getOutputStream());
 			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
 			
 			while (input.read(buffer) != -1) {
 				buffer.flip();
 				output.write(buffer);
 				buffer.clear();
 			}
 			
 			input.close();
 			output.close();
 		} catch (Exception e) {
 			log.error("Failed generating summary for patient " + patientId + " ... ", e);
 		}
 	}
 }
