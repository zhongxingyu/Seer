 package org.jboss.pressgangccms.rest.v1.components;
 
 import org.jboss.pressgangccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTLanguageImageV1;
 
 public class ComponentImageV1
 {
 	private final RESTImageV1 source;
 
 	public ComponentImageV1(final RESTImageV1 source)
 	{
 		this.source = source;
 	}
 
 	public static String getXMLTemplate(final RESTImageV1 source)
 	{
 		final String retValue = "<figure>\n" +
 			"\t<title>Title</title>\n" +
 			"\t<mediaobject>\n" +
 			"\t\t<imageobject>\n" +
			"\t\t\t<imagedata align=\"center\" fileref=\"images/" + getDocbookFileName(source) +"/>\n" +
 			"\t\t</imageobject>\n" +
 			"\t\t<textobject>\n" +
 			"\t\t\t<phrase>Description</phrase>\n" +
 			"\t\t</textobject>\n" +
 			"\t</mediaobject>\n" +
 		"</figure>";
 		
 		return retValue;
 	}
 	
 	public String getXMLTemplate()
 	{
 		return getXMLTemplate(source);
 	}
 	
 	/**
 	 * The docbook file name for an image is the ID of the image and the
 	 * extension of the first language image. This does imply that you can not
 	 * mix and match image formats within a single image.
 	 * 
 	 * @param source
 	 *            The RESTImageV1 object to get the docbook filename for
 	 * @return The docbook file name
 	 */
 	public static String getDocbookFileName(final RESTImageV1 source)
 	{
 		if (source == null)
 			throw new IllegalArgumentException("source cannot be null");
 
 		if (source.getLanguageImages_OTM() == null || source.getLanguageImages_OTM().getItems() == null || source.getLanguageImages_OTM().getItems().isEmpty())
 			return "";
 
 		for (final RESTLanguageImageV1 langImage : source.getLanguageImages_OTM().getItems())
 		{
 			final String filename = langImage.getFilename();
 			if (filename != null)
 			{
 				final int indexOfExtension = filename.lastIndexOf('.');
 
 				if (indexOfExtension != -1 && indexOfExtension < filename.length() - 1)
 				{
 					final String extension = filename.substring(indexOfExtension, filename.length());
 					return source.getId() + extension;
 				}
 			}
 		}
 
 		return "";
 	}
 
 	public String getDocbookFileName()
 	{
 		return getDocbookFileName(source);
 	}
 }
