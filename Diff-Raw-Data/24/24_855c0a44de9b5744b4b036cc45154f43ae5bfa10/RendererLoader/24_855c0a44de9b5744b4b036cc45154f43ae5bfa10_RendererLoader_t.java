 package org.antz29.jsbuilder;
 
 import java.io.File;
 
 import org.xeustechnologies.jcl.JarClassLoader;
 import org.xeustechnologies.jcl.JclObjectFactory;
 import org.xeustechnologies.jcl.JclUtils;
 
 public class RendererLoader {
 	
 	private String cls;
 	private File jar;
 	
 	public void setClass(String cls)
 	{
 		this.cls = cls;
 	}
 	
 	public void setJar(File jar)
 	{
 		this.jar = jar;
 	}
 	
 	public Renderer loadRenderer()
 	{
		Renderer renderer = null;
		
		if (this.jar == null) {
			ClassLoader loader = this.getClass().getClassLoader();
			
			try {
				renderer = (Renderer) loader.loadClass(this.cls).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			return renderer;
		}
		
 		JarClassLoader jcl = new JarClassLoader();
 		
 		jcl.add(jar.getAbsolutePath());		
 		JclObjectFactory factory = JclObjectFactory.getInstance();
 		Object rend = factory.create(jcl,cls);
		renderer = (Renderer) JclUtils.toCastable(rend, Renderer.class);
 		
 		return renderer;
 	}
 
 	
 }
