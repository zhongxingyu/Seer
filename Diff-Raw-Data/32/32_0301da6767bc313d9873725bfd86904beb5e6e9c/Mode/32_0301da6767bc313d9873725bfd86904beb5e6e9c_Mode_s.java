 package org.ndx.lifestream.rendering;
 
 import org.ndx.lifestream.rendering.output.gollum.GollumOutputter;
 import org.ndx.lifestream.rendering.output.gumdrop.GumdropOutputter;
 import org.ndx.lifestream.rendering.output.jbake.JBakeOutputter;
 import org.ndx.lifestream.rendering.output.jekyll.JekyllOutputter;
 
 /**
  * Defines the various usable rendering modes. Each mode define how obejcts are
  * to be transformed into objects that can be rendered using transformation
  * strategies. Obviously, each component has to write its own transformation
  * strategy, that I hope to be simpler than direct rendering.
  *
  * @author ndx
  *
  */
 public enum Mode {
 	/**
 	 * Infos here https://github.com/gollum/gollum/wiki
 	 */
 	gollum,
 	/**
 	 * Infos here http://gumdropapp.com/ (look specifically into http://gumdropapp.com/documentation.htm / "Page configuration")
 	 */
 	gumdrop,
 	/**
 	 * http://jekyllrb.com/docs/posts/
 	 */
 	jekyll,
 	/**
 	 * http://jbake.org/
 	 */
 	jbake, 
 	/**
 	 * http://www.opoopress.com/en/
 	 */
 	opoopress;
 
 	/**
 	 * Get writer for the given output file
 	 *
 	 * @param output
 	 * @return
 	 */
 	public OutputWriter getWriter() {
 		switch(this) {
 		case gollum:
 			return new GollumOutputter();
 		case gumdrop:
 			return new GumdropOutputter();
 		case jekyll:
 			return new JekyllOutputter();
 		case jbake:
 			return new JBakeOutputter();
 		case opoopress:
 			return new OpooPressOutputter();
 		default:
 			throw new UnsupportedOperationException("output using \""+name()+"\" has not been implemented");
 		}
 	}
 }
