 /**
  * Phresco Framework Implementation
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.framework.docs.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.BadPdfFormatException;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfCopy;
 import com.itextpdf.text.pdf.PdfImportedPage;
 import com.itextpdf.text.pdf.PdfReader;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.exception.PhrescoException;
 
 
 /**
  * Document Util to process PDF documents
  *
  */
 public final class DocumentUtil {
 	private static final Logger S_LOGGER = Logger.getLogger(DocumentUtil.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	private static String coreModule = "COREMODULE";
     private static String externalModule = "EXTERNALMODULE";
 	
     private DocumentUtil(){
 
     }
 
 
     /**
      * Adds title section.
      * @param info the project info object
      * @return PDF input stream
      * @throws PhrescoException 
      */
     public static InputStream getTitleSection(ApplicationInfo info) throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug(" Entering Method DocumentUtil.getTitleSection(ProjectInfo info)");
 		}
     	if (isDebugEnabled) {
     		S_LOGGER.debug("getTitleSection() projectCode=" + info.getCode());
 		}
         try {
 			//create output stream
 			com.itextpdf.text.Document docu = new com.itextpdf.text.Document();
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			PdfWriter.getInstance(docu, os);
 			docu.open();
 
 			//add standard title section with supplied info object
 			Paragraph paragraph = new Paragraph();
 			paragraph.setAlignment(Element.ALIGN_CENTER);
 			paragraph.setFont(DocConstants.TITLE_FONT);
 			addBlankLines(paragraph, MAGICNUMBER.DOCLINES);
 			paragraph.add(info.getName());
 			addBlankLines(paragraph, MAGICNUMBER.BLANKLINESFOUR);
 			docu.add(paragraph);
 			
 			paragraph = new Paragraph();
 			paragraph.setAlignment(Element.ALIGN_CENTER);
 			addBlankLines(paragraph, MAGICNUMBER.DOCLINES);
 			String techName = info.getTechInfo().getName();
 			if(StringUtils.isNotEmpty(info.getTechInfo().getVersion())) {
 				paragraph.add(techName + " - " + info.getTechInfo().getVersion());
 			} else {
 				paragraph.add(techName);
 			}
 			docu.add(paragraph);
 			
 			paragraph = new Paragraph();
 			addBlankLines(paragraph, MAGICNUMBER.DOCLINES);
 			paragraph.setAlignment(Element.ALIGN_CENTER);
 			paragraph.add(DocumentMessages.getString("Documents.version.name") + getVersion(info)); //$NON-NLS-1$
 			addBlankLines(paragraph, MAGICNUMBER.BLANKLINESSEVEN);
 			docu.add(paragraph);
 			
 			if(StringUtils.isNotEmpty(info.getDescription())) {
 				paragraph = new Paragraph();
 				paragraph.setAlignment(Element.ALIGN_RIGHT);
 				paragraph.setFont(DocConstants.DESC_FONT);
 				paragraph.setFirstLineIndent(MAGICNUMBER.BLANKLINESEIGHT);
 				docu.add(paragraph);
 
 			}
 			
 			docu.close();
 
 			//Create an inputstream to return.
 			return new ByteArrayInputStream(os.toByteArray());
 		} catch (DocumentException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 
     }
 
 
     /**
      * @param info
      * @return
      */
     private static String getVersion(ApplicationInfo info) {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.getVersion(ProjectInfo info)");
 		}
     	if (isDebugEnabled) {
     		S_LOGGER.debug("getVersion() ProjectCode="+ info.getCode());
 		}
    	String version = "";
         if(org.apache.commons.lang.StringUtils.isEmpty(version)){
             return DocumentMessages.getString("Documents.default.version"); //$NON-NLS-1$
         }
         return version;
     }
 
     /**
      * Creates and returns PDF input stream for the supplied string.
      * @param string to be printed in the PDF
      * @return PDF input stream.
      * @throws PhrescoException
      */
     public static InputStream getStringAsPDF(String string) throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.getStringAsPDF(String string)");
 		}
     	try {
 			com.itextpdf.text.Document docu = new com.itextpdf.text.Document();
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			PdfWriter.getInstance(docu, os);
 			docu.open();
 			Paragraph paragraph = new Paragraph();
 			paragraph.setAlignment(Element.ALIGN_LEFT);
 			paragraph.setFirstLineIndent(MAGICNUMBER.INDENTLINE);
 			paragraph.add("\n"); //$NON-NLS-1$
 			paragraph.add(string);
 			paragraph.add("\n\n"); //$NON-NLS-1$
 			docu.add(paragraph);
 
 			docu.close();
 
 			//Create an inputstream to return.
 			return new ByteArrayInputStream(os.toByteArray());
 		} catch (DocumentException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 
     }
 
     /**
      * Process tuple beans to generate Documnets for a speific entity type.
      * @param dependencyManager dependency manager
      * @param modules list of tuple beans
      * @param type Entity type
      * @return PDF input stream.
      * @throws PhrescoException
      */
     public static InputStream getDocumentStream(List<ArtifactGroup> modules) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entering Method DocumentUtil.getDocumentStream(RepositoryManager repoManager,List<TupleBean> modules, EntityType type)");
         }
         try {
 			if(CollectionUtils.isNotEmpty(modules)){
 			    com.itextpdf.text.Document docu = new com.itextpdf.text.Document();
 			    ByteArrayOutputStream os = new ByteArrayOutputStream();
 			    PdfWriter writer = PdfWriter.getInstance(docu, os);
 			    docu.open();
 			    List<ArtifactGroup> coreModules = new ArrayList<ArtifactGroup>();
 			    List<ArtifactGroup> externalModules = new ArrayList<ArtifactGroup>();
 			    List<ArtifactGroup> jsLibraries = new ArrayList<ArtifactGroup>();
 			    List<ArtifactGroup> components = new ArrayList<ArtifactGroup>();
 			    for (ArtifactGroup artifactGroup : modules) {
 					if(artifactGroup.getType().name().equals(Type.FEATURE.name())) {
 						if(artifactGroup.getAppliesTo().get(0).isCore() == true) {
 							coreModules.add(artifactGroup);
 						} else {
 							externalModules.add(artifactGroup);
 						}
 					} else if(artifactGroup.getType().name().equals(Type.JAVASCRIPT.name())) {
 						jsLibraries.add(artifactGroup);
 					} else if(artifactGroup.getType().name().equals(Type.COMPONENT.name())) {
 						components.add(artifactGroup);
 					}
 				}
 			    
 				if (CollectionUtils.isNotEmpty(coreModules)) {
 					updateDoc(coreModules, docu, writer, coreModule);
 				}
 				if (CollectionUtils.isNotEmpty(externalModules)) {
 					updateDoc(externalModules, docu, writer, externalModule);
 				}
 				if (CollectionUtils.isNotEmpty(jsLibraries)) {
 					updateDoc(jsLibraries, docu, writer, "JsLibraries");
 				}
 				if (CollectionUtils.isNotEmpty(components)) {
 					updateDoc(components, docu, writer, "Components");
 				}
 			    docu.close();
 
 			    return new ByteArrayInputStream(os.toByteArray());
 			}
 		} catch (DocumentException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 		return null;
     }
 
     private static void updateDoc(List<ArtifactGroup> modules, com.itextpdf.text.Document docu, PdfWriter writer, String moduleName) throws PhrescoException {
 		try {
 			Paragraph para = new Paragraph();
 			para.setAlignment(Element.ALIGN_CENTER);
 			para.setFont(DocConstants.BODY_FONT);
 			para.setFont(DocConstants.CATEGORY_FONT);
 			para.add(moduleName);
 			addBlankLines(para, MAGICNUMBER.BLANKLINESTWO);
 			docu.add(para); 
 			
 		for (ArtifactGroup artifactGroup : modules) {
 		    para = new Paragraph();
 		    para.setFont(DocConstants.CATEGORY_FONT);
 		    para.add(artifactGroup.getName());
 		    docu.add(para);
 		    
 		    if(StringUtils.isNotEmpty(artifactGroup.getDescription())) {
 		    	 para = new Paragraph();
 		         para.setFont(DocConstants.BODY_FONT);
 		         para.add(artifactGroup.getDescription());
 		         addBlankLines(para, 2);
 		         docu.add(para);
 		    }
 //		    Documentation document = tupleBean.getDoc(DocumentationType.DESCRIPTION);
 //		    if (document != null) {
 //		        if(!StringUtils.isEmpty(document.getUrl())){
 //		            PdfInput convertToPdf = DocConvertor.convertToPdf(document.getUrl());
 //		            if(convertToPdf != null) {
 //		                DocumentUtil.addPages(convertToPdf.getInputStream(), writer, docu);
 //		            }
 //		        } else {
 //		            para = new Paragraph();
 //		            para.setFont(DocConstants.BODY_FONT);
 //		            para.add(document.getContent());
 //		            addBlankLines(para, 2);
 //		            docu.add(para);
 //		        }
 //		    }
 		}
 		} catch (DocumentException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 	}
 
     
     
     /**
      * Adds blank lines into the supplied paragraph.
      * @param p the Paragraph object
      * @param noOfLines no of blank lines.
      */
     private static void addBlankLines(Paragraph p, int noOfLines) {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.addBlankLines(Paragraph p, int noOfLines)");
 		}
     	if (isDebugEnabled) {
     		S_LOGGER.debug("addBlankLines() No of Lines="+noOfLines);
 		}
     	StringBuffer sb =new StringBuffer();
         for (int i = 0; i < noOfLines; i++) {
             sb.append("\n"); //$NON-NLS-1$
         }
         p.add(sb.toString());
     }
 
     /**
      * @param titleSection
      * @param pdfCopy
      * @throws PhrescoException 
      */
     public static void addPages(InputStream titleSection, PdfCopy pdfCopy) throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.addPages(InputStream titleSection, PdfCopy pdfCopy)");
 		}
     	try {
 			PdfReader reader = new PdfReader(titleSection);
 			reader.consolidateNamedDestinations();
 			int pages = reader.getNumberOfPages();
 			for (int i = 1; i <= pages; i++) {
 			    PdfImportedPage importedPage = pdfCopy.getImportedPage(reader, i);
 			    pdfCopy.addPage(importedPage);
 			}
 			pdfCopy.freeReader(reader);
 		} catch (BadPdfFormatException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
     }
 
 
     /**
      * @param titleSection
      * @param writer
      * @param docu
      * @throws PhrescoException 
      */
     public static void addPages(InputStream titleSection, PdfWriter writer, com.itextpdf.text.Document docu) throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.addPages(InputStream titleSection, PdfWriter writer, com.itextpdf.text.Document docu)");
 		}
     	try {
 			PdfReader reader = new PdfReader(titleSection);
 			reader.consolidateNamedDestinations();
 			PdfContentByte cb = writer.getDirectContent();
 
 			int pages = reader.getNumberOfPages();
 			for (int i = 1; i <= pages; i++) {
 			    PdfImportedPage importedPage = writer.getImportedPage(reader, i);
 			    cb.addTemplate(importedPage, 0, 0);
 			    docu.newPage();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
     }
 
 
     /**
      * @param dependencyManager
      * @param tuples
      * @param type
      * @param pdfCopy
      * @return
      * @throws PhrescoException
      */
     public static InputStream addPages(List<ArtifactGroup> tuples, PdfCopy pdfCopy)
             throws PhrescoException {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.addPages(RepositoryManager repoManager,List<TupleBean> tuples, EntityType type, PdfCopy pdfCopy)");
 		}
     	InputStream addDocumentInfo = getDocumentStream(tuples);
 		if(addDocumentInfo != null) {
 		    addPages(addDocumentInfo, pdfCopy);
 		}
 		return addDocumentInfo;
     }
 
     /**
      * @param folder
      * @return
      */
     public static String getIndexHtml(File folder) {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.getIndexHtml(File folder)");
 		}
     	if (isDebugEnabled) {
     		S_LOGGER.debug("getIndexHtml() folder=" + folder.getPath());
 		}
     	StringBuffer sb = new StringBuffer();
         sb.append("<html>"); //$NON-NLS-1$
         sb.append("<body>"); //$NON-NLS-1$
         sb.append("<h1>");  //$NON-NLS-1$
         sb.append(DocumentMessages.getString("Documents.document.index.title")); //$NON-NLS-1$
         sb.append("</h1>");  //$NON-NLS-1$
         listFiles(folder, sb);
         sb.append("</body>"); //$NON-NLS-1$
         sb.append("</html>"); //$NON-NLS-1$
         return sb.toString();
     }
 
     private static void listFiles(File file,StringBuffer sb) {
     	if (isDebugEnabled) {
     		S_LOGGER.debug("Entering Method DocumentUtil.listFiles(File file,StringBuffer sb)");
 		}
     	String[] list = file.list();
         sb.append("<ul>"); //$NON-NLS-1$
         for (String fileOrFolder : list) {
             File newFile = new File(file.toString() + File.separator + fileOrFolder);
             if(newFile.isHidden()) { continue; }
             /*if(newFile.isDirectory()){
                 sb.append("<li>"); //$NON-NLS-1$
                 sb.append("<a href=./"); //$NON-NLS-1$
                 sb.append(newFile.getName());
                 sb.append("\">"); //$NON-NLS-1$
                 sb.append(newFile.getPath());
                 sb.append("</a>"); //$NON-NLS-1$
                 sb.append("</li>"); //$NON-NLS-1$
                 listFiles(newFile, sb);
             } else {*/
                 sb.append("<li>"); //$NON-NLS-1$
                 sb.append("<a href="+"\""+"./"); //$NON-NLS-1$
                 sb.append(newFile.getName());
                 sb.append("\">"); //$NON-NLS-1$
                 sb.append(newFile.getName());
                 sb.append("</a>"); //$NON-NLS-1$
                 sb.append("</li>"); //$NON-NLS-1$
 //            }
         }
         sb.append("</ul>"); //$NON-NLS-1$
     }
 }
