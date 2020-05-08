 package net.vidageek.autoweb.taglib.core;
 
 import net.vidageek.autoweb.taglib.support.ContextDependentPath;
 import net.vidageek.autoweb.taglib.support.TagEnvironment;
 import net.vidageek.autoweb.taglib.support.VersionedPath;
 
 final public class JsTagCore implements AutowebSimpleTag {
 
 	private final VersionedPath path;
 
 	public JsTagCore(final String path) {
 		this.path = new VersionedPath(path);
 	}
 
 	public void applyTo(final TagEnvironment env) {
 		env.write("<script type=\"text/javascript\" src=\""
				+ new ContextDependentPath(path.toString(), env.contextPath()).toString() + "\" ></script>");
 	}
 
 }
