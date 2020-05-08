 package twigkit.fig.jsp;
 
 import twigkit.fig.Config;
 import twigkit.fig.Fig;
 import twigkit.fig.Value;
 import twigkit.fig.loader.PropertiesLoader;
 
 /**
  * @author mr.olafsson
  */
 public class Functions {
 
 	public static Fig load(String config) {
 		return Fig.load(new PropertiesLoader(config));
 	}
 
 	public static Config get(Fig fig, String conf) {
 		return fig.get(conf.split("_"));
 	}
 
 	public static Value value(Config conf, String name) {
 		for (Value v : conf.values()) {
 			System.out.println(v.label() + " " + v.as_string());
 		}
 		System.out.println("==> " + conf.value(name));

 		return conf.value(name);
 	}
 }
