 /*
  * Copyright (c) 2008 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.eandb.jmist.framework.shader.ray;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Stack;
 
 import ca.eandb.jmist.framework.Illuminable;
 import ca.eandb.jmist.framework.Intersection;
 import ca.eandb.jmist.framework.Light;
 import ca.eandb.jmist.framework.LightSample;
 import ca.eandb.jmist.framework.Material;
 import ca.eandb.jmist.framework.Medium;
 import ca.eandb.jmist.framework.Modifier;
 import ca.eandb.jmist.framework.NearestIntersectionRecorder;
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.framework.RayShader;
 import ca.eandb.jmist.framework.ScatteredRay;
 import ca.eandb.jmist.framework.ScatteredRays;
 import ca.eandb.jmist.framework.Scene;
 import ca.eandb.jmist.framework.SceneElement;
 import ca.eandb.jmist.framework.Shader;
 import ca.eandb.jmist.framework.ShadingContext;
 import ca.eandb.jmist.framework.ScatteredRay.Type;
 import ca.eandb.jmist.framework.color.Color;
 import ca.eandb.jmist.framework.color.ColorModel;
 import ca.eandb.jmist.framework.color.WavelengthPacket;
 import ca.eandb.jmist.framework.random.SimpleRandom;
 import ca.eandb.jmist.math.Basis3;
 import ca.eandb.jmist.math.Point2;
 import ca.eandb.jmist.math.Point3;
 import ca.eandb.jmist.math.Ray3;
 import ca.eandb.jmist.math.Vector3;
 
 /**
  * @author brad
  *
  */
 public final class SceneRayShader implements RayShader {
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = -1662354927701351486L;
 
 	private final Light light;
 
 	private final SceneElement root;
 
 	private final RayShader background;
 
 	private final Random rng;
 
 	/**
 	 * @param caster
 	 * @param light
 	 * @param background
 	 * @param rng
 	 */
 	public SceneRayShader(SceneElement root, Light light, RayShader background, Random rng) {
 		this.root = root;
 		this.light = light;
 		this.background = background;
 		this.rng = rng;
 	}
 
 	/**
 	 * @param caster
 	 * @param light
 	 * @param background
 	 */
 	public SceneRayShader(SceneElement root, Light light, RayShader background) {
 		this(root, light, background, new SimpleRandom());
 	}
 
 	/**
 	 * @param caster
 	 * @param light
 	 * @param background
 	 */
 	public SceneRayShader(Scene scene, RayShader background) {
 		this(scene.getRoot(), scene.getLight(), background, new SimpleRandom());
 	}
 
 	/**
 	 * @param caster
 	 * @param light
 	 * @param background
 	 */
 	public SceneRayShader(Scene scene) {
 		this(scene.getRoot(), scene.getLight(), RayShader.BLACK, new SimpleRandom());
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.RayShader#shadeRay(ca.eandb.jmist.math.Ray3, ca.eandb.jmist.framework.color.WavelengthPacket)
 	 */
 	public Color shadeRay(Ray3 ray, WavelengthPacket lambda) {
 		Context context = new Context();
 		return context.castPrimaryRay(ray, lambda);
 	}
 
 	private final class LocalContext {
 
 		public double distance;
 		public Color importance;
 		public Ray3 ray;
 		public List<LightSample> samples;
 		public Modifier modifier;
 		public ScatteredRays scatteredRays;
 		public Shader shader;
 		public boolean front;
 		public Basis3 basis;
 		public Basis3 shadingBasis;
 		public Point3 position;
 		public Point2 uv;
 		public Medium medium;
 		public Material material;
 		public int primitiveIndex;
 
 	}
 
 	private class Context implements ShadingContext, Illuminable {
 
 		private final Stack<LocalContext> stack = new Stack<LocalContext>();
 		private final EnumMap<ScatteredRay.Type, Integer> depth = new EnumMap<ScatteredRay.Type, Integer>(ScatteredRay.Type.class);
 		private int totalDepth = 0;
 		private final Stack<Medium> media = new Stack<Medium>();
 
 		public Color castPrimaryRay(Ray3 ray, WavelengthPacket lambda) {
 			Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, root);
 
 			if (x != null) {
 				LocalContext local = new LocalContext();
 				local.ray = ray;
 				local.distance = x.getDistance();
 				local.front = x.isFront();
 				local.medium = Medium.VACUUM;
 				local.importance = lambda.getColorModel().getWhite(lambda);
 
 				stack.push(local);
 				x.prepareShadingContext(this);
 
 				local.scatteredRays = new ScatteredRays(this, ray.direction(), getWavelengthPacket(), rng, local.material);
 
 				Color color = shade();
 
 				stack.pop();
 				return color;
 			} else {
 				return background.shadeRay(ray, lambda);
 			}
 		}
 
 		public Color castRay(ScatteredRay sr) {
 			ScatteredRay.Type type = sr.getType();
 			Ray3 ray = sr.getRay();
 			Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, root);
 
 			if (x != null) {
 				totalDepth++;
 				depth.put(type, getPathDepthByType(type) + 1);
 
 				boolean pop = false;
				if (isFront() == sr.isTransmitted()) {
 					media.push(getMaterial());
 					pop = true;
 				}
 
 				Medium popped = null;
 				Medium medium;
 				if (x.isFront()) {
 					medium = media.isEmpty() ? Medium.VACUUM : media.peek();
 				} else {
 					popped = media.isEmpty() ? null : media.pop();
 					medium = popped != null ? popped : Medium.VACUUM;
 				}
 
 				LocalContext local = new LocalContext();
 				local.ray = ray;
 				local.distance = x.getDistance();
 				local.front = x.isFront();
 				local.medium = medium;
 				local.importance = sr.getColor().times(stack.peek().importance);
 
 				stack.push(local);
 				x.prepareShadingContext(this);
 
 				local.scatteredRays = new ScatteredRays(this, ray.direction(), getWavelengthPacket(), rng, local.material);
 
 				Color color = shade();
 				color = color.times(medium.transmittance(local.ray,
 						local.distance, color.getWavelengthPacket()));
 
 				if (popped != null) {
 					media.push(popped);
 				}
 
 				if (pop) {
 					media.pop();
 				}
 
 				stack.pop();
 				depth.put(type, depth.get(type) - 1);
 				totalDepth--;
 				return color;
 			} else {
 				return background.shadeRay(ray, getWavelengthPacket());
 			}
 		}
 
 		public Color getAmbientLight() {
 			// TODO add ambient illumination from scene
 			return getColorModel().getBlack(getWavelengthPacket());
 		}
 
 		public ColorModel getColorModel() {
 			return getImportance().getColorModel();
 		}
 
 		public WavelengthPacket getWavelengthPacket() {
 			return getImportance().getWavelengthPacket();
 		}
 
 		public Color getImportance() {
 			return stack.peek().importance;
 		}
 
 		public Iterable<LightSample> getLightSamples() {
 			List<LightSample> samples = stack.peek().samples;
 			if (samples == null) {
 				samples = new ArrayList<LightSample>();
 				stack.peek().samples = samples;
 				light.illuminate(this, getWavelengthPacket(), rng, this);
 			}
 			return samples;
 		}
 
 		public int getPathDepth() {
 			return totalDepth;
 		}
 
 		public int getPathDepthByType(Type type) {
 			Integer d = depth.get(type);
 			return (d != null ? d.intValue() : 0);
 		}
 
 		public ScatteredRays getScatteredRays() {
 			return stack.peek().scatteredRays;
 		}
 
 		public Color shade() {
 			return getShader().shade(this);
 		}
 
 		public double getDistance() {
 			return stack.peek().distance;
 		}
 
 		public Vector3 getIncident() {
 			return stack.peek().ray.direction();
 		}
 
 		public boolean isFront() {
 			return stack.peek().front;
 		}
 
 		public Basis3 getBasis() {
 			return stack.peek().basis;
 		}
 
 		public Vector3 getNormal() {
 			return stack.peek().basis.w();
 		}
 
 		public Point3 getPosition() {
 			return stack.peek().position;
 		}
 
 		public Basis3 getShadingBasis() {
 			return stack.peek().shadingBasis;
 		}
 
 		public Vector3 getShadingNormal() {
 			return stack.peek().shadingBasis.w();
 		}
 
 		public Vector3 getTangent() {
 			return stack.peek().basis.u();
 		}
 
 		public Point2 getUV() {
 			return stack.peek().uv;
 		}
 
 		public boolean visibility(Ray3 ray) {
 			return root.visibility(ray);
 		}
 
 		public void addLightSample(LightSample sample) {
 			List<LightSample> samples = stack.peek().samples;
 			assert(samples != null);
 			samples.add(sample);
 		}
 
 		public Modifier getModifier() {
 			return stack.peek().modifier;
 		}
 
 		public Random getRandom() {
 			return rng;
 		}
 
 		public Ray3 getRay() {
 			return stack.peek().ray;
 		}
 
 		public Shader getShader() {
 			return stack.peek().shader;
 		}
 
 		public void setAmbientMedium(Medium medium) {
 			stack.peek().medium = medium;
 		}
 
 		public void setBasis(Basis3 basis) {
 			stack.peek().basis = basis;
 		}
 
 		public void setMaterial(Material material) {
 			stack.peek().material = material;
 		}
 
 		public void setModifier(Modifier modifier) {
 			stack.peek().modifier = modifier;
 		}
 
 		public void setNormal(Vector3 normal) {
 			stack.peek().basis = Basis3.fromW(normal);
 		}
 
 		public void setPosition(Point3 position) {
 			stack.peek().position = position;
 		}
 
 		public void setPrimitiveIndex(int index) {
 			stack.peek().primitiveIndex = index;
 		}
 
 		public void setShader(Shader shader) {
 			stack.peek().shader = shader;
 		}
 
 		public void setShadingBasis(Basis3 basis) {
 			stack.peek().shadingBasis = basis;
 		}
 
 		public void setShadingNormal(Vector3 normal) {
 			stack.peek().shadingBasis = Basis3.fromW(normal);
 		}
 
 		public void setUV(Point2 uv) {
 			stack.peek().uv = uv;
 		}
 
 		public Medium getAmbientMedium() {
 			return stack.peek().medium;
 		}
 
 		public Material getMaterial() {
 			return stack.peek().material;
 		}
 
 		public int getPrimitiveIndex() {
 			return stack.peek().primitiveIndex;
 		}
 
 	}
 
 }
