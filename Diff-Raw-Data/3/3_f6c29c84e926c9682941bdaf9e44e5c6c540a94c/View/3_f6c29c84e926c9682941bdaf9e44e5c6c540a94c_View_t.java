 package gui;
 
 import gui.MainPanel.JmolPanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import main.PropHandler;
 import main.ScreenSetup;
 import main.Settings;
 
 import org.jmol.export.dialog.Dialog;
 import org.jmol.util.BoxInfo;
 import org.jmol.viewer.Viewer;
 
 import util.ArrayUtil;
 import util.FileUtil;
 import util.SequentialWorkerThread;
 import util.Vector3fUtil;
 import cluster.Model;
 
 public class View
 {
 	private Viewer viewer;
 	GUIControler guiControler;
 	public static View instance;
 
 	public static int FONT_SIZE = 10;
 	public boolean antialias = false;
 
 	private static Zoomable zoomedTo;
 
 	public static enum AnimationSpeed
 	{
 		SLOW, FAST
 	}
 
 	private View(Viewer viewer, GUIControler guiControler, boolean hideHydrogens)
 	{
 		this.viewer = viewer;
 		this.guiControler = guiControler;
 
 		viewer.script("set disablePopupMenu on");
 		viewer.script("set minPixelSelRadius 30");
 
 		setAntialiasOn(ScreenSetup.SETUP.isAntialiasOn());
 
 		hideHydrogens(hideHydrogens);
 	}
 
 	public static void init(JmolPanel jmolPanel, GUIControler guiControler, boolean hideHydrogens)
 	{
 		instance = new View((Viewer) jmolPanel.getViewer(), guiControler, hideHydrogens);
 	}
 
 	public synchronized void setAntialiasOn(boolean antialias)
 	{
 		this.antialias = antialias;
 		if (antialias)
 			viewer.script("set antialiasDisplay ON");
 		else
 			viewer.script("set antialiasDisplay OFF");
 	}
 
 	public boolean isAntialiasOn()
 	{
 		return antialias;
 	}
 
 	public synchronized void setSpinEnabled(boolean spinEnabled)
 	{
 		if (spinEnabled)
 		{
 			viewer.evalString("set spinx 0");
 			viewer.evalString("set spiny 3");
 			viewer.evalString("set spinz 0");
 			viewer.evalString("spin on");
 		}
 		else
 		{
 			viewer.evalString("spin off");
 		}
 	}
 
 	public synchronized void zoomTo(Zoomable zoomable, AnimationSpeed speed)
 	{
 		zoomTo(zoomable, speed, null);
 	}
 
 	public synchronized void zoomTo(final Zoomable zoomable, final AnimationSpeed speed, Boolean superimposed)
 	{
 		if (superimposed == null)
 			superimposed = zoomable.isSuperimposed();
 		final float diameter = zoomable.getDiameter(superimposed);
 		final Vector3f center = zoomable.getCenter(superimposed);
 
 		Settings.LOGGER.debug("Superimposed " + superimposed);
 		Settings.LOGGER.debug("Center       " + center);
 		Settings.LOGGER.debug("Diameter     " + diameter);
 		//		Settings.LOGGER.debug("Rot radius   " + viewer.getRotationRadius());
 
		int zoom = (int) ((1200 / (10 / viewer.getRotationRadius())) / diameter);

 		//		Settings.LOGGER.warn("zoom " + zoom);
 		zoom = (int) Math.max(5, zoom);
 
 		if (isAnimated())
 		{
 			guiControler.block("zoom to " + Vector3fUtil.toString(center));
 			final int finalZoom = zoom;
 			sequentially(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					boolean setAntialiasBackOn = false;
 					if (ScreenSetup.SETUP.isAntialiasOn() && isAntialiasOn())
 					{
 						setAntialiasBackOn = true;
 						setAntialiasOn(false);
 					}
 					String cmd = "zoomto " + (speed == AnimationSpeed.SLOW ? 0.66 : 0.33) + " "
 							+ Vector3fUtil.toString(center) + " " + finalZoom;
 					viewer.scriptWait(cmd);
 					if (setAntialiasBackOn)
 						setAntialiasOn(true);
 					zoomedTo = zoomable;
 					guiControler.unblock("zoom to " + Vector3fUtil.toString(center));
 				}
 			}, "zoom to " + zoomable);
 		}
 		else
 		{
 			String cmd = "zoomto 0 " + Vector3fUtil.toString(center) + " " + zoom;
 			zoomedTo = zoomable;
 			viewer.scriptWait(cmd);
 		}
 
 	}
 
 	public Zoomable getZoomTarget()
 	{
 		return zoomedTo;
 	}
 
 	public synchronized static String convertColor(Color col)
 	{
 		return "[" + col.getRed() + ", " + col.getGreen() + ", " + col.getBlue() + "]";
 	}
 
 	public synchronized static String convertPos(Point3f p)
 	{
 		return "{" + p.x + " " + p.y + ", " + p.z + "}";
 	}
 
 	public synchronized void setBackground(Color col)
 	{
 		viewer.script("background " + convertColor(col));
 	}
 
 	public synchronized int findNearestAtomIndex(int x, int y)
 	{
 		return viewer.findNearestAtomIndexFixed(x, y);
 	}
 
 	public synchronized int getAtomModelIndex(int atomIndex)
 	{
 		return viewer.getAtomModelIndex(atomIndex);
 	}
 
 	public synchronized void clearSelection()
 	{
 		viewer.clearSelection();
 	}
 
 	public synchronized void select(BitSet bitSet)
 	{
 		//		Settings.LOGGER.warn("XX> selecting bitset: " + bitSet);
 		viewer.select(bitSet, false, null, false);
 		//Settings.LOGGER.warn("XX> " + viewer.getAtomSetCenter(bitSet));
 	}
 
 	private void evalScript(String script)
 	{
 		if (script.matches("(?i).*hide.*") || script.matches("(?i).*subset.*") || script.matches("(?i).*display.*"))
 			throw new Error("use wrap methods");
 	}
 
 	public synchronized void scriptWait(String script)
 	{
 		evalScript(script);
 		//		Settings.LOGGER.warn("XX> " + script);
 		viewer.scriptWait(script);
 	}
 
 	public synchronized void evalString(String script)
 	{
 		evalScript(script);
 		//		Settings.LOGGER.warn("XX> " + script);
 		viewer.evalString(script);
 	}
 
 	public synchronized void selectAll()
 	{
 		viewer.scriptWait("select not hidden");
 	}
 
 	public synchronized void hide(BitSet bs)
 	{
 		//		Settings.LOGGER.warn("XX> hide " + bs);
 		viewer.select(bs, false, null, false);
 		hideSelected();
 	}
 
 	public synchronized void hideSelected()
 	{
 		//		Settings.LOGGER.warn("XX> select selected OR hidden; hide selected");
 		viewer.scriptWait("select selected OR hidden; hide selected");
 	}
 
 	public synchronized void display(BitSet bs)
 	{
 		//		Settings.LOGGER.warn("XX> display " + bs);
 		viewer.select(bs, false, null, false);
 		viewer.scriptWait("select (not hidden) OR selected; select not selected; hide selected");
 	}
 
 	public synchronized BitSet getModelUndeletedAtomsBitSet(int modelIndex)
 	{
 		return viewer.getModelUndeletedAtomsBitSet(modelIndex);
 	}
 
 	public synchronized String getModelNumberDotted(int i)
 	{
 		return viewer.getModelNumberDotted(i);
 	}
 
 	public synchronized Point3f getAtomSetCenter(BitSet bitSet)
 	{
 		return viewer.getAtomSetCenter(bitSet);
 	}
 
 	HashSet<Model> spheresForModel = new HashSet<Model>();
 	public double sphereSize = 0.5;
 	public double sphereTranslucency = 0.5;
 
 	public synchronized void hideSphere(Model m)
 	{
 		if (spheresForModel.contains(m))
 		{
 			String id = "sphere" + m.getModelIndex();
 			scriptWait("ellipsoid ID " + id + " color translucent 1.0");
 		}
 	}
 
 	private synchronized void updateSpherePosition(Model m)
 	{
 		if (spheresForModel.contains(m))
 		{
 			String id = "sphere" + m.getModelIndex();
 
 			//			BoxInfo info = viewer.getBoxInfo(m.getBitSet(), 1.0F);
 			//				Point3f center = info.getBoundBoxCenter();
 			//				Vector3f corner = info.getBoundBoxCornerVector();
 			//				scriptWait("ellipsoid ID " + id + " AXES {" + Math.max(corner.x * sphereSize, 1.0) + " 0 0} {0 "
 			//						+ Math.max(corner.y * sphereSize, 1.0) + " 0} {0 0 " + Math.max(corner.z * sphereSize, 1.0)
 			//						+ "}");
 			//				scriptWait("ellipsoid ID " + id + " center " + convertPos(center));
 
 			double size = Math.max(1.0, getDiameter(m.getBitSet()) * 0.5 * (0.1 + 0.9 * sphereSize));
 			scriptWait("ellipsoid ID " + id + " AXES {" + size + " 0 0} {0 " + size + " 0} {0 0 " + size + "}");
 			scriptWait("ellipsoid ID " + id + " center " + convertPos(getAtomSetCenter(m.getBitSet())));
 		}
 	}
 
 	public synchronized void showSphere(Model m, boolean updateSizeAndPos)
 	{
 		String id = "sphere" + m.getModelIndex();
 		if (!spheresForModel.contains(m) || updateSizeAndPos)
 		{
 			spheresForModel.add(m);
 			updateSpherePosition(m);
 		}
 
 		double trans = 0.0 + 0.8 * sphereTranslucency;
 		switch (m.getTranslucency())
 		{
 			case ModerateWeak:
 				trans = 0.2 + 0.65 * sphereTranslucency;
 				break;
 			case ModerateStrong:
 				trans = 0.4 + 0.5 * sphereTranslucency;
 				break;
 			case Strong:
 				trans = 0.6 + 0.35 * sphereTranslucency;
 		}
 		scriptWait("ellipsoid ID " + id + " " + m.getHighlightColor() + " color translucent " + trans);
 	}
 
 	public synchronized void zap(boolean b, boolean c, boolean d)
 	{
 		viewer.zap(b, c, d);
 	}
 
 	public synchronized void loadModelFromFile(String s, String filename, String s2[], Object reader, boolean b,
 			Hashtable<String, Object> t, StringBuffer sb, int i)
 	{
 		viewer.loadModelFromFile(s, filename, s2, reader, b, t, sb, i);
 	}
 
 	public synchronized int getModelCount()
 	{
 		return viewer.getModelCount();
 	}
 
 	public synchronized void setAtomCoordRelative(Vector3f c, BitSet bitSet)
 	{
 		viewer.setAtomCoordRelative(c, bitSet);
 	}
 
 	public synchronized void setAtomCoordRelative(final List<Vector3f> c, final List<BitSet> bitSet,
 			final AnimationSpeed overlapAnim)
 	{
 		if (isAnimated() && c.size() > 1)
 		{
 			guiControler.block("spread cluster " + c);
 			sequentially(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					int n = (overlapAnim == AnimationSpeed.SLOW) ? 24 : 10;
 					for (int i = 0; i < n; i++)
 					{
 						for (int j = 0; j < bitSet.size(); j++)
 						{
 							Vector3f v = new Vector3f(c.get(j));
 							v.scale(1 / (float) n);
 							viewer.setAtomCoordRelative(v, bitSet.get(j));
 						}
 						viewer.scriptWait("delay 0.01");
 					}
 					guiControler.unblock("spread cluster " + c);
 				}
 			}, "move bitsets");
 		}
 		else
 		{
 			for (int i = 0; i < bitSet.size(); i++)
 				viewer.setAtomCoordRelative(c.get(i), bitSet.get(i));
 		}
 	}
 
 	public synchronized void setAtomProperty(BitSet bitSet, int temperature, int v, float v2, String string, float f[],
 			String s[])
 	{
 		viewer.setAtomProperty(bitSet, temperature, v, v2, string, f, s);
 	}
 
 	public synchronized int getAtomCountInModel(int index)
 	{
 		return viewer.getAtomCountInModel(index);
 	}
 
 	public synchronized BitSet getSmartsMatch(String smarts, BitSet bitSet)
 	{
 		BitSet b = viewer.getSmartsMatch(smarts, bitSet);
 		if (b == null)
 		{
 			Settings.LOGGER.warn("jmol did not like: " + smarts + " " + bitSet);
 			return new BitSet();
 		}
 		else
 			return b;
 	}
 
 	HashSet<String> animSuspend = new HashSet<String>();
 
 	public synchronized void suspendAnimation(String key)
 	{
 		if (animSuspend.contains(key))
 			throw new Error("already suspended animation for: " + key);
 		animSuspend.add(key);
 	}
 
 	public synchronized void proceedAnimation(String key)
 	{
 		if (!animSuspend.contains(key))
 			throw new Error("use suspend first for " + key);
 		animSuspend.remove(key);
 	}
 
 	public synchronized boolean isAnimated()
 	{
 		return animSuspend.size() == 0;
 	}
 
 	SequentialWorkerThread swt = new SequentialWorkerThread();
 
 	private void sequentially(final Runnable r, final String name)
 	{
 		if (swt.runningInThread())
 			r.run();
 		else
 			swt.addJob(r, name);
 	}
 
 	public void afterAnimation(final Runnable r, final String name)
 	{
 		if (isAnimated())
 			swt.addJob(r, name);
 		else
 			r.run();
 	}
 
 	public synchronized void hideHydrogens(boolean b)
 	{
 		scriptWait("set showHydrogens " + (b ? "FALSE" : "TRUE"));
 	}
 
 	public float getDiameter(BitSet bitSet)
 	{
 		List<Vector3f> points = new ArrayList<Vector3f>();
 		for (int i = 0; i < bitSet.size(); i++)
 			if (bitSet.get(i))
 				points.add(new Vector3f(viewer.getAtomPoint3f(i)));
 		Vector3f[] a = new Vector3f[points.size()];
 		return Vector3fUtil.maxDist(points.toArray(a));
 	}
 
 	public List<Vector3f> getCenterAndAxes(BitSet bitSet)
 	{
 		BoxInfo info = viewer.getBoxInfo(bitSet, 1.0F);
 		Point3f center = info.getBoundBoxCenter();
 		Vector3f corner = info.getBoundBoxCornerVector();
 
 		System.out.println(info);
 		System.out.println(corner);
 		System.out.println(ArrayUtil.toString(info.getBboxVertices()));
 
 		return null;
 	}
 
 	HashMap<Dimension, Dimension> cachedResolutions = new HashMap<Dimension, Dimension>();
 
 	/**
 	 * Copied from org.openscience.jmol.app.jmolpanel.JmolPanel
 	 */
 	public void exportImage()
 	{
 		int qualityJPG = -1;
 		int qualityPNG = -1;
 		String imageType = null;
 		String[] imageChoices = { "JPEG", "PNG", "GIF", "PPM", "PDF" };
 		String[] imageExtensions = { "jpg", "png", "gif", "ppm", "pdf" };
 		Dialog sd = new Dialog();
 		String dir = PropHandler.get("image-export-dir");
 		if (dir == null)
 			dir = System.getProperty("user.home");
 		String name = dir + File.separator + "ches-mapper-image.jpg";
 		String fileName = sd.getImageFileNameFromDialog(viewer, name, imageType, imageChoices, imageExtensions,
 				qualityJPG, qualityPNG);
 		if (fileName == null)
 			return;
 		PropHandler.put("image-export-dir", FileUtil.getParent(fileName));
 		PropHandler.storeProperties();
 		qualityJPG = sd.getQuality("JPG");
 		qualityPNG = sd.getQuality("PNG");
 		String sType = imageType = sd.getType();
 		if (sType == null)
 		{
 			// file type changer was not touched
 			sType = fileName;
 			int i = sType.lastIndexOf(".");
 			if (i < 0)
 				return; // make no assumptions - require a type by extension
 			sType = sType.substring(i + 1).toUpperCase();
 		}
 		Dimension resScreen = new Dimension(viewer.getScreenWidth(), viewer.getScreenHeight());
 		Dimension resCached = cachedResolutions.get(resScreen);
 		if (resCached == null)
 			resCached = resScreen;
 		Dimension resSelected = ResolutionPanel.getResuloution(Settings.TOP_LEVEL_FRAME, "Select Image Resolution",
 				(int) resCached.getWidth(), (int) resCached.getHeight());
 		if (resSelected == null)
 			return;
 		cachedResolutions.put(resScreen, resSelected);
 		Settings.LOGGER.info((String) viewer.createImage(fileName, sType, null, sd.getQuality(sType),
 				resSelected.width, resSelected.height));
 	}
 
 	public synchronized void selectFirstCarbonAtom(BitSet bs)
 	{
 		//System.out.println(empty.cardinality());
 		int firstAtom = -1;
 		int firstCarbon = -1;
 		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
 		{
 			//System.out.println(i + " " + viewer.getAtomInfo(i));
 			if (firstAtom == -1)
 				firstAtom = i;
 			if (viewer.getAtomInfo(i).matches("^C[0-9].*"))
 			{
 				firstCarbon = i;
 				break;
 			}
 		}
 		if (firstCarbon == -1)
 			firstCarbon = firstAtom;
 		//		System.out.println("atom to select: " + viewer.getAtomInfo(firstCarbon));
 
 		BitSet sel = new BitSet(bs.length());
 		sel.set(firstCarbon);
 		select(sel);
 	}
 }
