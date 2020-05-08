 package com.maton.tools.stiletto.process;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 
 import com.maton.tools.crusher.Crusher;
 import com.maton.tools.stiletto.model.Alternate;
 import com.maton.tools.stiletto.model.Bundle;
 import com.maton.tools.stiletto.model.CharMetric;
 import com.maton.tools.stiletto.model.Font;
 import com.maton.tools.stiletto.model.Image;
 import com.maton.tools.stiletto.model.Positioned;
 import com.maton.tools.stiletto.model.Resolution;
 import com.maton.tools.stiletto.model.Sprite;
 import com.maton.tools.stiletto.model.base.IImageProvider;
 import com.maton.tools.stiletto.model.io.ModelOutput;
 import com.maton.tools.stiletto.process.base.ImagePart;
 
 public class BundleBuilder implements IRunnableWithProgress {
 
 	protected Resolution resolution;
 	protected String bundleName;
 	protected String prefix;
 	protected Bundle bundle;
 	protected IProgressMonitor monitor;
 	protected ArrayList<ImagePart> parts;
 	protected HashMap<String, ArrayList<ImagePart>> fontChars;
 	protected String pngFile;
 	protected String bundleFile;
 	protected String atlasFile;
 	protected String cssFile;
 
 	public BundleBuilder(Bundle bundle, Resolution res) {
 		this.resolution = res;
 		this.bundle = bundle;
 		parts = new ArrayList<ImagePart>();
 		fontChars = new HashMap<String, ArrayList<ImagePart>>();
 
 		prefix = "main_";
 		bundleName = "Main";
 
 		if (resolution != null) {
 			bundleName = resolution.getName();
 			prefix = resolution.getName() + "_";
 		}
 	}
 
 	@Override
 	public void run(IProgressMonitor monitor) throws InvocationTargetException,
 			InterruptedException {
 		this.monitor = monitor;
 
 		cssFile = prefix + bundle.getName() + ".css";
 		pngFile = prefix + bundle.getName() + ".png";
 		bundleFile = prefix + bundle.getName() + ".xml";
 		atlasFile = prefix + bundle.getName() + "_atlas.xml";
 
 		startup();
 		buildFonts();
 		distribute();
 		purge();
 		writeXml();
 		unload();
 	}
 
 	private void writeXml() {
 		monitor.beginTask("Generating Bundle: " + bundleName, 10);
 
 		monitor.subTask("Writing Texture Atlas...");
 		ModelOutput.saveAtlas(
 				new File(bundle.getCtx().getBuildPath(atlasFile)), pngFile,
 				parts);
 
 		monitor.subTask("Writing CSS Sprites...");
 		ModelOutput.saveCss(new File(bundle.getCtx().getBuildPath(cssFile)),
 				bundle.getName(), pngFile, parts);
 
 		for (Font font : bundle.getFonts().getList()) {
 			monitor.subTask("Writing Font " + font.getName() + "...");
 
 			File fontFile = new File(bundle.getCtx().getBuildPath(
 					prefix + font.getName() + ".fnt"));
			font.setResolution(resolution);
 			ModelOutput.saveFont(fontFile, font, fontChars.get(font.getName()));
			font.setResolution(null);
 		}
 
 		monitor.subTask("Writing the final Bundle...");
 		ModelOutput.saveBundle(new File(bundle.getCtx()
 				.getBuildPath(bundleFile)), prefix, atlasFile, bundle);
 	}
 
 	private void purge() {
 		// Purge fonts
 		for (int i = parts.size() - 1; i >= 0; i--) {
 			if (parts.get(i).getName() == null) {
 				parts.remove(i);
 			}
 		}
 	}
 
 	private void distribute() {
 		monitor.beginTask("Consolidating resources", 10);
 
 		monitor.subTask("Optimizing Texture Atlas...");
 
 		Crusher crusher = new Crusher();
 		for (ImagePart part : parts) {
 			crusher.addPart(part);
 		}
 
 		crusher.run();
 
 		monitor.subTask("Saving image...");
 		BufferedImage img = (BufferedImage) crusher.buildImage();
 		try {
 			ImageIO.write(img, "png",
 					new File(bundle.getCtx().getBuildPath(pngFile)));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void startup() {
 		monitor.beginTask("Preparing resources", 10);
 
 		monitor.subTask("Preparing images for the target bundle...");
 		for (Image img : bundle.getImages().getList()) {
 			img.setResolution(resolution);
 		}
 
 		monitor.subTask("Preparing sprites for the target bundle...");
 		for (Sprite spr : bundle.getSprites().getList()) {
 			for (Positioned pos : spr.getList()) {
 				pos.setResolution(resolution);
 			}
 		}
 
 		monitor.subTask("Preparing subtextures...");
 		List<IImageProvider> used = ModelOutput.getUsedImages(bundle);
 		for (IImageProvider img : used) {
 			String name = "";
 
 			Alternate alt = null;
 			if (img instanceof Image) {
 				name = ((Image) img).getExportName();
 
 				if (resolution != null) {
 					alt = ((Image) img).getAlternates().getElement(
 							resolution.getName());
 				}
 			} else if (img instanceof Sprite) {
 				name = ((Sprite) img).getImageName();
 			}
 
 			ImagePart part = null;
 			if (resolution == null) {
 				part = new ImagePart(name, img, img.getImage());
 			} else {
 				if (alt != null) {
 					java.awt.Image alti = bundle.getCtx().loadAwtAlternate(
 							alt.getImageName() + ".png");
 					part = new ImagePart(name, img, alti);
 				} else {
 					float scale = resolution.getScale();
 
 					part = new ImagePart(name, img, resizedImage(
 							img.getImage(), scale));
 				}
 			}
 			parts.add(part);
 		}
 	}
 
 	public void unload() {
 		monitor.beginTask("Unloading resources", 10);
 
 		monitor.subTask("Deleting temporal images...");
 
 		for (ImagePart part : parts) {
 			part.getImage().flush();
 		}
 		parts.clear();
 	}
 
 	public void buildFonts() {
 		List<Font> fonts = bundle.getFonts().getList();
 		for (Font font : fonts) {
 			buildFont(font);
 		}
 	}
 
 	public void buildFont(Font font) {
 		monitor.subTask("Building font " + font.getName() + "...");
 
 		ArrayList<ImagePart> chars = new ArrayList<ImagePart>();
 
 		float scale = 1;
 
 		if (resolution != null) {
 			scale = resolution.getScale();
 		}
 
 		List<CharMetric> metrics = font.getCharMetrics(resolution);
 		for (CharMetric metric : metrics) {
 			BufferedImage charImg = new BufferedImage(metric.width,
 					metric.height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
 			Graphics2D g = charImg.createGraphics();
 			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			font.regenFont(g, resolution);
 			font.renderChar(g, 0, 0, metric);
 			g.dispose();
 
 			java.awt.Image wi = charImg;
 			if (font.hasAlternate(resolution) == false && scale != 1) {
 				wi = resizedImage(charImg, scale);
 				metric.x = (int)(scale * (float)metric.x);
 				metric.y = (int)(scale * (float)metric.y);
 				metric.width = wi.getWidth(null);
 				metric.height = wi.getHeight(null);
 				metric.xadvance = (int)(scale * (float)metric.xadvance);
 				metric.lineHeight = (int)(scale * (float)metric.lineHeight);
 			}
 			
 			ImagePart part = new ImagePart(null, metric, wi);
 			parts.add(part);
 
 			chars.add(part);
 		}
 
 		fontChars.put(font.getName(), chars);
 	}
 
 	public java.awt.Image resizedImage(java.awt.Image img, float scale) {
 		if (scale == 1) {
 			return img;
 		}
 
 		int ow = img.getWidth(null);
 		int oh = img.getHeight(null);
 		int tw = (int) ((float) img.getWidth(null) * scale);
 		int th = (int) ((float) img.getHeight(null) * scale);
 
 		if (tw == 0 || th == 0) {
 			System.out.println(ow + "," + oh + " -> " + tw + "," + th);
 
 			if (tw == 0) {
 				tw = 1;
 			}
 
 			if (th == 0) {
 				th = 1;
 			}
 		}
 
 		BufferedImage sizedImg = new BufferedImage(tw, th,
 				BufferedImage.TYPE_4BYTE_ABGR_PRE);
 		Graphics2D g = sizedImg.createGraphics();
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
 				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
 				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
 
 		g.drawImage(img, 0, 0, tw, th, 0, 0, ow, oh, null);
 
 		g.dispose();
 
 		return sizedImg;
 	}
 }
