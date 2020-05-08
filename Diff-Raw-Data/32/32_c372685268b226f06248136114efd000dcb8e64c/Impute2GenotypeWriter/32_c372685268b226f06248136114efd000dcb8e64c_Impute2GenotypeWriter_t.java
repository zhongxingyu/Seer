 package org.molgenis.genotype.impute2;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.molgenis.genotype.Allele;
 import org.molgenis.genotype.Alleles;
 import org.molgenis.genotype.GenotypeData;
 import org.molgenis.genotype.Sample;
 import org.molgenis.genotype.Sample.SampleAnnotation.Type;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.NotASnpException;
 import org.molgenis.genotype.variant.SampleVariantsProvider;
 
 /**
  * Export a GenotypeData object to an impute2 haps/sample files
  * 
  * @author erwin
  * 
  */
 public class Impute2GenotypeWriter
 {
 	public static final Charset FILE_ENCODING = Charset.forName("UTF-8");
 	public static final String LINE_ENDING = "\n";
 	private static final char SEPARATOR = ' ';
 	private static final char UNPHASED_INDICATOR = '*';
 	private static final char MISSING_INDICATOR = '?';
 	private static final Logger LOG = Logger.getLogger(Impute2GenotypeWriter.class);
 	private GenotypeData genotypeData;
 
 	public Impute2GenotypeWriter(GenotypeData genotypeData)
 	{
 		this.genotypeData = genotypeData;
 	}
 
 	public void write(String basePath) throws IOException
 	{
 		write(new File(basePath + ".haps"), new File(basePath + ".sample"));
 	}
 
 	public void write(File hapsFile, File sampleFile) throws IOException
 	{
 		LOG.info("Writing haps file [" + hapsFile.getAbsolutePath() + "] and sample file ["
 				+ sampleFile.getAbsolutePath() + "]");
 
 		Writer hapsFileWriter = null;
 		Writer sampleFileWriter = null;
 		try
 		{
 			hapsFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hapsFile), FILE_ENCODING));
 			sampleFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sampleFile),
 					FILE_ENCODING));
 			write(hapsFileWriter, sampleFileWriter);
 		}
 		finally
 		{
 			IOUtils.closeQuietly(hapsFileWriter);
 			IOUtils.closeQuietly(sampleFileWriter);
 		}
 	}
 
 	public void write(Writer hapsFileWriter, Writer sampleFileWriter) throws IOException
 	{
 		writeHapsFile(hapsFileWriter);
 		writeSampleFile(sampleFileWriter);
 	}
 
 	private void writeSampleFile(Writer sampleWriter) throws IOException
 	{
 		// Write headers
 		StringBuilder sb = new StringBuilder();
 		sb.append("ID_1");
 		sb.append(SEPARATOR);
 		sb.append("ID_2");
 		sb.append(SEPARATOR);
 		sb.append("missing");
 
 		List<Sample> samples = genotypeData.getSamples();
 		List<String> headers = getSampleHeaders(samples);
 
 		for (String header : headers)
 		{
 			sb.append(SEPARATOR);
 			sb.append(header);
 		}
 
 		sb.append(LINE_ENDING);
 
 		// Write datatypes
 		sb.append("0");
 		sb.append(SEPARATOR);
 		sb.append("0");
 		sb.append(SEPARATOR);
 		sb.append("0");
 
 		for (String dataType : getDataTypes(headers, samples))
 		{
 			sb.append(SEPARATOR);
 			sb.append(dataType);
 		}
 
 		sb.append(LINE_ENDING);
 		sampleWriter.write(sb.toString());
 
 		for (Sample sample : samples)
 		{
 			sb = new StringBuilder();
 			sb.append(sample.getFamilyId() == null ? "NA" : sample.getFamilyId());
 			sb.append(SEPARATOR);
 			sb.append(sample.getId() == null ? "NA" : sample.getId());
 			sb.append(SEPARATOR);
 			sb.append(getValue("missing", sample, "0"));
 
 			for (String header : headers)
 			{
 				sb.append(SEPARATOR);
 				sb.append(getValue(header, sample, "NA"));
 			}
 
 			sb.append(LINE_ENDING);
 			sampleWriter.write(sb.toString());
 		}
 	}
 
 	private String getValue(String header, Sample sample, String nullValue)
 	{
 		Object value = sample.getAnnotations().get(header).getValue();
 		if (value == null)
 		{
 			return nullValue;
 		}
 
 		if (value instanceof Boolean)
 		{
 			return value.equals(true) ? "1" : "0";
 		}
 
		if (value instanceof Double || value instanceof Float)
		{
			String result = value.toString();
			if (result.endsWith("0"))
			{
				result = result.substring(0, result.length() - 1);
			}

			return result;
		}

 		return value.toString();
 	}
 
 	private List<String> getDataTypes(List<String> headers, List<Sample> samples)
 	{
 		List<String> dataTypes = new ArrayList<String>();
 
 		for (String header : headers)
 		{
 			boolean found = false;
 			for (int i = 0; i < samples.size() && !found; i++)
 			{
 				Map<String, Sample.SampleAnnotation> annotations = samples.get(i).getAnnotations();
 				if (annotations.containsKey(header))
 				{
 					Sample.SampleAnnotation annotation = annotations.get(header);
 					Object value = annotation.getValue();
 
 					if ((value != null) && (!value.equals("NA")))
 					{
 						if (value instanceof Boolean)
 						{
 							dataTypes.add("B");
 						}
 						else if (value instanceof Integer)
 						{
 							dataTypes.add("D");
 						}
 						else if (value instanceof Double || value instanceof Float)
 						{
 							String dataType = annotation.getType() == Type.COVARIATE ? "C" : "P";
 							dataTypes.add(dataType);
 						}
 						else
 						{
 							LOG.warn("Annotation with key [" + header + "] is of an unsupported data type");
 						}
 
 						found = true;
 					}
 
 				}
 			}
 		}
 
 		return dataTypes;
 	}
 
 	private List<String> getSampleHeaders(List<Sample> samples)
 	{
 		List<String> headers = new ArrayList<String>();
 
 		for (Sample sample : samples)
 		{
 			for (String header : sample.getAnnotations().keySet())
 			{
 				if (!header.equalsIgnoreCase("missing"))
 				{
 					if (sample.getAnnotations().get(header).getType() == Type.OTHER)
 					{
 						LOG.warn("Annotation with key [" + header + "] is of an unsuppoprted type");
 					}
 					else if (!headers.contains(header))
 					{
 						headers.add(header);
 					}
 				}
 			}
 		}
 
 		return headers;
 	}
 
 	private void writeHapsFile(Writer hapsFileWriter) throws IOException
 	{
 		for (GeneticVariant variant : genotypeData)
 		{
 			if (!variant.isSnp())
 			{
 				throw new NotASnpException(variant);
 			}
 
 			Allele allele0 = variant.getVariantAlleles().get(0);
 			Allele allele1 = variant.getVariantAlleles().get(1);
 
 			StringBuilder sb = new StringBuilder();
 			sb.append(variant.getSequenceName());
 			sb.append(SEPARATOR);
 			sb.append(variant.getPrimaryVariantId());
 			sb.append(SEPARATOR);
 			sb.append(variant.getStartPos());
 			sb.append(SEPARATOR);
 			sb.append(allele0);
 			sb.append(SEPARATOR);
 			sb.append(allele1);
 
 			SampleVariantsProvider variantsProvider = variant.getSampleVariantsProvider();
 			List<Alleles> sampleAlleles = variantsProvider.getSampleVariants(variant);
 			List<Boolean> phasing = variantsProvider.getSamplePhasing(variant);
 
 			if ((sampleAlleles != null) && !sampleAlleles.isEmpty())
 			{
 				for (int i = 0; i < sampleAlleles.size(); i++)
 				{
 					sb.append(SEPARATOR);
 
 					Alleles alleles = sampleAlleles.get(i);
 					if ((alleles == null) || alleles.getAllelesAsString().isEmpty() || (alleles.get(0) == Allele.ZERO)
 							|| (alleles.get(1) == Allele.ZERO))
 					{
 						sb.append(MISSING_INDICATOR);
 						sb.append(SEPARATOR);
 						sb.append(MISSING_INDICATOR);
 					}
 					else
 					{
 						if (alleles.get(0).equals(allele0))
 						{
 							sb.append("0");
 						}
 						else if (alleles.get(0).equals(allele1))
 						{
 							sb.append("1");
 						}
 						else
 						{
 							throw new RuntimeException("SampleAllele [" + alleles.get(0) + "] for SNP ["
 									+ variant.getPrimaryVariantId() + "] does not match one of the variant alleles");
 						}
 
 						if (!phasing.get(i))
 						{
 							sb.append(UNPHASED_INDICATOR);
 						}
 
 						sb.append(SEPARATOR);
 
 						if (alleles.get(1).equals(allele0))
 						{
 							sb.append("0");
 						}
 						else if (alleles.get(1).equals(allele1))
 						{
 							sb.append("1");
 						}
 						else
 						{
 							throw new RuntimeException("SampleAllele [" + alleles.get(1) + "] for SNP ["
 									+ variant.getPrimaryVariantId() + "] does not match one of the variant alleles");
 						}
 
 						if (!phasing.get(i))
 						{
 							sb.append(UNPHASED_INDICATOR);
 						}
 					}
 				}
 			}
 
 			sb.append(LINE_ENDING);
 
 			hapsFileWriter.write(sb.toString());
 		}
 	}
 }
