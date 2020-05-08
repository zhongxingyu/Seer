 package rosa.tool.deriv;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import javax.xml.transform.stream.StreamResult;
 
 import org.xml.sax.SAXException;
 
 import rosa.core.BookArchive;
 import rosa.core.BookCollection;
 import rosa.tool.Config;
 import rosa.core.util.XMLWriter;
 
 /**
  * Write out config files for FSI server.
  */
 public class WebsiteFSIDerivative extends Derivative {
 	public static String NAME = "website-fsi";
 
 	public WebsiteFSIDerivative(Config site, PrintStream report)
 			throws IOException {
 		super(site, report);
 	}
 
 	public String name() {
 		return NAME;
 	}
 
 	public boolean update(BookArchive archive, boolean force) {
 		try {
 			writeFSIPagesConfig(archive,
 					site.dataPath(archive.id(), archive.id() + ".pages.fsi"));
 			writeFSIShowcaseConfig(archive,
 					site.dataPath(archive.id(), archive.id() + ".showcase.fsi"));
 		} catch (SAXException e) {
 			reportError("Writing fsi config", e);
 			return false;
 		} catch (IOException e) {
 			reportError("Writing fsi config", e);
 			return false;
 		}
 
 		return true;
 	}
 
 	private void writeFSIPagesConfig(BookArchive archive, File file)
 			throws SAXException, IOException {
 		List<BookArchive.Image> images = archive.images();
 
 		int start = -1;
 		int end = -1;
 		String frontcover = null;
 		String backcover = null;
 
 		for (int i = 0; i < images.size(); i++) {
 			BookArchive.Image image = images.get(i);
 
 			if (image.fileName().contains("binding.frontcover")) {
 				if (!image.missing()) {
 					frontcover = image.fileName();
 				}
 
 				start = i + 1;
 			} else if (image.fileName().contains("binding.backcover")) {
 				if (!image.missing()) {
 					backcover = image.fileName();
 				}
 
 				end = i;
 			}
 		}
 
 		XMLWriter w = new XMLWriter(new StreamResult(file));
 		w.startDocument();
 
 		w.startElement("fsi_parameter");
 
 		w.startElement("PLUGINS");
 
 		w.attribute("src", "pages");
 		w.startElement("PLUGIN");
 
 		w.attribute("value", "0");
 		w.emptyElement("BendEffectIntensity");
 
 		w.attribute("value", "0");
 		w.emptyElement("Print");
 
 		w.attribute("value", "0");
 		w.emptyElement("Save");
 
 		w.attribute("value", "0");
 		w.emptyElement("Search");
 
 		File cropdir = archive.cropDir();
 		boolean hascrop = cropdir.exists();
 
 		String basepath = site.fsiServerShare() + "/" + archive.id() + "/"
 				+ (hascrop ? cropdir.getName() + "/" : "");
 
 		if (frontcover == null) {
 			w.attribute("value", "true");
 			w.emptyElement("BlankFrontCover");
 		} else {
 			w.attribute("value", basepath + frontcover);
 			w.emptyElement("FrontCoverImage");
 		}
 
 		if (backcover == null) {
 			w.attribute("value", "true");
 			w.emptyElement("BlankBackCover");
 		} else {
 			w.attribute("value", basepath + backcover);
 			w.emptyElement("BackCoverImage");
 		}
 
 		w.endElement("PLUGIN");
 
 		w.attribute("src", "fullscreen");
 		w.emptyElement("PLUGIN");
 
 		w.attribute("src", "resize");
 		w.emptyElement("PLUGIN");
 
 		w.endElement("PLUGINS");
 
 		w.startElement("Images");
 
 		for (int i = start; i < end; i++) {
 			BookArchive.Image image = images.get(i);
 
 			String filename = image.fileName();
 			String label = filename.replace(".tif", "");
 			label = label.replace(archive.id() + ".", "");
 			String imagepath = basepath + filename;
 
 			if (image.missing()) {
 				imagepath = site.fsiServerShare() + "/"
 						+ BookCollection.MISSING_IMAGE_NAME;
 			} else if (!new File(archive.dir(), filename).exists()
 					|| (hascrop && !new File(cropdir, filename).exists())) {
 				report.println("Error: Missing image: " + filename
 						+ (hascrop ? " (cropped)" : ""));
 			}
 
 			w.attribute("label", label);
 			w.startElement("Image");
 			w.startElement("FPX");
 			w.attribute("value", imagepath);
 			w.emptyElement("Src");
 			w.endElement("FPX");
 			w.endElement("Image");
 		}
 
 		w.endElement("Images");
 		w.endElement("fsi_parameter");
 		w.endDocument();
 	}
 
 	private void writeFSIShowcaseConfig(BookArchive archive, File file)
 			throws SAXException, IOException {
 		XMLWriter w = new XMLWriter(new StreamResult(file));
 		w.startDocument();
 
 		w.startElement("fsi_parameter");
 
 		w.startElement("PLUGINS");
 		w.attribute("src", "showcase");
 		w.startElement("PLUGIN");
 
 		w.attribute("value", "11");
 		w.emptyElement("LabelTextSize");
 
 		w.attribute("value", "2");
 		w.emptyElement("LabelMarginTop");
 
 		w.attribute("value", "<b><ImageLabel /></b>");
 		w.emptyElement("LabelContent");
 
 		w.attribute("value", "000000");
 		w.emptyElement("LabelTextColor");
 
 		w.attribute("value", "outside");
 		w.emptyElement("Layout");
 
 		w.attribute("value", "75");
 		w.emptyElement("BackgroundAlpha");
 
 		w.attribute("value", "true");
 		w.emptyElement("Tooltips");
 
 		w.attribute("value", "bottom");
 		w.emptyElement("Align");
 
 		w.attribute("value", "CCCCCC");
 		w.emptyElement("ThumbFace");
 
 		w.attribute("value", "F2984C");
 		w.emptyElement("ThumbActiveFace");
 
 		w.attribute("value", "FFF0AA");
 		w.emptyElement("ThumbSelectedFace");
 
		String basepath = site.fsiServerShare() + "/" + archive.id() + "/";
 
 		w.endElement("PLUGIN");
 
 		w.attribute("src", "fullscreen");
 		w.emptyElement("PLUGIN");
 
 		w.attribute("src", "resize");
 		w.emptyElement("PLUGIN");
 
 		w.endElement("PLUGINS");
 
 		w.startElement("Images");
 
 		for (BookArchive.Image image : archive.images()) {
 			if (!image.missing()) {
 				String label = image.fileName().replace(".tif", "");
 				label = label.replace(archive.id() + ".", "");
 
 				w.attribute("label", label);
 				w.startElement("Image");
 
 				w.startElement("FPX");
 				w.attribute("value", basepath + image.fileName());
 				w.emptyElement("Src");
 				w.endElement("FPX");
 				w.endElement("Image");
 			}
 		}
 
 		w.endElement("Images");
 		w.endElement("fsi_parameter");
 		w.endDocument();
 	}
 
 	// TODO
 	public boolean check(BookArchive archive) {
 		return false;
 	}
 
 	public boolean validate(BookArchive archive) {
 		return check(archive);
 	}
 }
