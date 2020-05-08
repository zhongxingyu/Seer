 package wicket.contrib.scriptaculous.effects;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import wicket.Component;
 import wicket.contrib.scriptaculous.JavascriptBuilder;
 
 public interface Effect
 {
 
 	String toJavascript();
 
 	public abstract class AbstractEffect implements Effect
 	{
 		private final Component component;
 		private final Map<String, Object> options;
 
 		public AbstractEffect(Component component)
 		{
 			this.component = component;
 			this.options = new HashMap<String, Object>();
 		}
 
 		public AbstractEffect(Component component, Map<String, Object> options)
 		{
 			this.component = component;
 			this.options = options;
 		}
 
 		public String toString()
 		{
 			return toJavascript();
 		}
 
 		public String toJavascript()
 		{
 			JavascriptBuilder builder = new JavascriptBuilder();
 			builder
 					.addLine("new Effect." + getEffectName() + "('" + component.getMarkupId()
							+ "' ");
 			builder.addOptions(options);
 			builder.addLine(");");
 
 			return builder.toJavascript();
 		}
 
 		protected void addOption(String key, Object value)
 		{
 			options.put(key, value);
 		}
 
 		protected abstract String getEffectName();
 
 	}
 
 	/**
 	 * 
 	 * @see http://wiki.script.aculo.us/scriptaculous/show/Effect.Highlight
 	 */
 	public class Highlight extends AbstractEffect
 	{
 
 		public Highlight(Component component)
 		{
 			super(component);
 		}
 
 		public Highlight(Component component, Map<String, Object> options)
 		{
 			super(component, options);
 		}
 
 		@Override
 		protected String getEffectName()
 		{
 			return "Highlight";
 		}
 
 		public void setStartColor(String rgb)
 		{
 			addOption("startcolor", rgb);
 		}
 
 		public void setEndColor(String rgb)
 		{
 			addOption("endcolor", rgb);
 		}
 
 		public void setRestoreColor(String rgb)
 		{
 			addOption("restorecolor", rgb);
 		}
 	}
 }
