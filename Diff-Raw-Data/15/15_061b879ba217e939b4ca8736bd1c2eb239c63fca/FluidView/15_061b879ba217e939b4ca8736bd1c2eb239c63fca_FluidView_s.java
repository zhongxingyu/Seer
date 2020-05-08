 package com.htssoft.sploosh.presentation;
 
 import java.io.IOException;
 import java.util.ArrayDeque;
 import java.util.List;
 
 import com.htssoft.sploosh.TracerAdvecter;
 import com.jme3.effect.shapes.EmitterShape;
 import com.jme3.export.JmeExporter;
 import com.jme3.export.JmeImporter;
 import com.jme3.math.FastMath;
 import com.jme3.math.Transform;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.Control;
 
 /**
  * This is a Geometry designed to show a view of a VortonSpace fluid simulation.
  * 
  * By default, it will also manage stepping the simulation forward.
  * */
 public class FluidView extends Geometry {
 	protected FluidTracerMesh tracerMesh;
 	protected TracerAdvecter fluid;
 	protected int nTracers;
 	protected boolean enableSim = true;
 	protected float reynoldsRatio = 1f;
 	protected FluidTracerAffector affector;
 	protected FluidTracerInitializer init;
 	protected EmitterShape emitterShape;
 	protected boolean burstMode = true;
 	protected ArrayDeque<Integer> freeIndexes;
 	protected float streamPerSec = 0f;
 	protected float streamAccum = 0f;
 	protected float particleLife = 0f;
 	
 	public FluidView(int nTracers, TracerAdvecter fluid){
 		this.fluid = fluid;
 		this.nTracers = nTracers;
 		freeIndexes = new ArrayDeque<Integer>(nTracers);
 		init();
 	}
 	
 	protected void init(){
 		this.setShadowMode(ShadowMode.Off);
 		this.setQueueBucket(Bucket.Transparent);
 		this.setIgnoreTransform(true);
 		
 		this.setMesh(tracerMesh = new FluidTracerMesh(nTracers));
 		this.addControl(new FluidControl());
 		this.setCullHint(CullHint.Never);
 		
 		if (fluid.hasDriver()){
 			enableSim = false;
 			System.out.println("Not driving.");
 		}
 		else {
 			fluid.setHasDriver(true);
 			System.out.println("Driving.");
 		}		
 	}
 	
 	public void setReynoldsRatio(float r){
 		reynoldsRatio = FastMath.clamp(r, 0f, 1f);
 	}
 	
 	public void enableSim(boolean enable){
 		this.enableSim = enable;
 	}
 	
 	public void toggleSim(){
 		this.enableSim = !this.enableSim;
 	}
 	
 	public void setScale(Vector3f scale){
 		tracerMesh.setScale(scale);
 	}
 	
 	public void setStreamPerSec(float perSec){
 		if (perSec > 0f){
 			burstMode = false;
 			streamPerSec = perSec;
 		}
 		else {
 			burstMode = true;
 		}
 	}
 		
 	public void distributeTracers(Vector3f center, float radius, float lifetime){
 		Transform trans = this.getWorldTransform();
 		
 		List<FluidTracer> tracers = tracerMesh.getBuffer();
 		for (FluidTracer t : tracers){
 			t.lifetime = lifetime;
 			t.position.set(randomComponent(), randomComponent(), randomComponent());
 			t.position.normalizeLocal();
 			t.position.multLocal(radius * FastMath.nextRandomFloat());
 			t.position.addLocal(center);
 			t.reynoldsRatio = reynoldsRatio;
 			trans.transformVector(t.position, t.position);
 		}
 	}
 	
 	/**
 	 * Set the emitter shape.
 	 * */
 	public void setShape(EmitterShape shape){
 		this.emitterShape = shape;
 	}
 	
 	/**
 	 * Sets the tracer affector.
 	 * */
 	public void setAffector(FluidTracerAffector affector){
 		this.affector = affector;
 	}
 	
 	public void setInit(FluidTracerInitializer init){
 		this.init = init;
 	}
 	
 	public void setParticleLife(float lifetime){
 		particleLife = lifetime;
 	}
 	
 	public void initializeTracers(float lifetime){
 		setParticleLife(lifetime);
 		initializeTracers();
 	}
 	
 	/**
 	 * Initialize tracers from the current shape and affector.
 	 * */
 	public void initializeTracers(){
 		if (init == null || emitterShape == null){
 			throw new IllegalStateException("To initialize tracers this way you must have both a shape and an init set.");
 		}
 		Transform trans = this.getWorldTransform();
 		
 		
 		FluidTracer[] tracers = tracerMesh.getBufferArray();
 		for (int i = 0; i < tracers.length; i++){
 			initTracer(tracers, i, trans);
 		}
 	}
 	
 	protected void initTracer(FluidTracer[] tracers, int index, Transform trans){
 		FluidTracer t = tracers[index];
 		t.lifetime = particleLife;
 		t.reynoldsRatio = reynoldsRatio;
 		init.initTracer(t, emitterShape, trans);
 	}
 	
 	protected float randomComponent(){
 		return FastMath.nextRandomFloat() * (FastMath.nextRandomFloat() < 0.5f ? -1 : 1);
 	}
 	
 	protected boolean emitStreamParticle(FluidTracer[] tracers, Transform trans){
 		Integer unusedIndex = freeIndexes.poll();
 		if (unusedIndex == null){
 			return false;
 		}
 		
 		
 		initTracer(tracers, unusedIndex, trans);
 		
 		return true;
 	}
 	
 	protected void updateStream(float tpf){
 		streamAccum += streamPerSec * tpf;
 		
 		Transform trans = getWorldTransform();
 		FluidTracer[] tracers = tracerMesh.getBufferArray();
 		
 		for (int i = 0; i < tracers.length; i++){
			if (tracers[i].age > tracers[i].lifetime || tracers[i].lifetime <= 0f){
 				freeIndexes.add(i);
 			}
 		}
 		
 		for (; streamAccum >= 1f && emitStreamParticle(tracers, trans); streamAccum -= 1f);
 	}
 	
 	public void updateFromControl(float tpf){
 		fluid.updateTransform(getWorldTransform());
 		
		if (enableSim){
			fluid.stepSimulation(tpf);
		}

 		if (!burstMode){
 			updateStream(tpf);
 		}
		
 		
 		FluidTracer[] buffer = tracerMesh.getBufferArray();
 		
 		if (affector != null){
 			for (FluidTracer t : buffer){
 				affector.affectTracer(t, tpf);
 			}
 		}
 		
 		fluid.advectTracers(buffer, tpf);
 		tracerMesh.updateBuffers();
 		this.setBoundRefresh();
 		
 	}
 	
 	public void renderFromControl(){
 	}
 	
 	private class FluidControl implements Control {
 
 		@Override
 		public void write(JmeExporter ex) throws IOException {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void read(JmeImporter im) throws IOException {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Control cloneForSpatial(Spatial spatial) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void setSpatial(Spatial spatial) {
 		}
 
 		@Override
 		public void setEnabled(boolean enabled) {
 		}
 
 		@Override
 		public boolean isEnabled() {
 			return true;
 		}
 
 		@Override
 		public void update(float tpf) {
 			updateFromControl(tpf);
 		}
 
 		@Override
 		public void render(RenderManager rm, ViewPort vp) {
 			renderFromControl();
 		}
 		
 	}
 }
