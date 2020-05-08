 package pb.tps.writer;
 
 import java.util.LinkedList;
 import java.util.SortedSet;
 
 import org.apache.velocity.VelocityContext;
 
 import pb.tps.model.Group;
 import pb.tps.model.ItemLink;
 import pb.tps.model.VeloAware;
 import pb.tps.reader.LocationsXLSReader;
 
 import com.google.common.collect.Lists;
 
 public class LocationsHTMLWriter extends VeloAware {
 	private LocationsXLSReader reader = null;
 	private int tablew;
 	private int tdw;
 	private String url;
 	private String mapUrl;
 
 	public LocationsHTMLWriter(LocationsXLSReader reader, String fileName,
 			int tablew, int tdw, String url, String mapUrl) {
 		super(fileName);
 		this.reader = reader;
 		this.tablew = tablew;
 		this.tdw = tdw;
 		this.url = url;
 		this.mapUrl = mapUrl;
 	}
 
 	public void run(int columns) {
 		StringBuilder acc = new StringBuilder();
 		VelocityContext context = new VelocityContext();
 		context.put("tablew", tablew);
 		context.put("tdw", tdw);
 		context.put("max", columns);
 		context.put("mapurl", mapUrl);
 		context.put("url", url);
 
 		acc.append(this.runVelo("locations_header.vm", context));
 
 		for (Group group : reader.getGroups()) {
 			SortedSet<ItemLink> srt = reader.getForSuper(group.getSupers());
 			context.put("group", group.getHeader());
 			context.put("supers", group.getSupers());
 
 			LinkedList<ItemLink> vls = Lists.newLinkedList(srt);
 
 			// We want to have same amount of table cells per row
 			int pad = srt.size() % columns;
 			// pad > 0 means that 1..(columns -1) need to be added
 			if (pad > 0) {
 				pad = columns - pad;
 				for (int i = 0; i < pad; i++) {
 					vls.add(new ItemLink(true));
 				}
 			}
 
 			context.put("subs", vls);
 			acc.append(this.runVelo("locations_contents.vm", context));
 		}
 
 		try {
 			acc.append("</body>\n</html>\n");
 			getWriter().write(acc.toString());
 			getWriter().flush();
 			getWriter().close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
