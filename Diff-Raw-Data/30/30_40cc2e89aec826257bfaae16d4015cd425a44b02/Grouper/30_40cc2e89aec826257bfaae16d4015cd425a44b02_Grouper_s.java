 package supersql.codegenerator;
 
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 public class Grouper implements Operator {
 
     int id; // SchemaID
 
     int Dimension; // 
 
     public TFE tfe; // TFE
 
     //hanki start
 	boolean order_flag;
 	boolean aggregate_flag;
 	String order;
     String aggregate;
     //hanki end
     
     public DecorateList decos = new DecorateList();
     
     public Grouper() {
     		//hanki start
 		order_flag = false;
 		aggregate_flag = false;
 	    //hanki end
 	    
         Dimension = -1;
     }
 
     public Grouper(int d) {
     		//hanki start
 		order_flag = false;
 		aggregate_flag = false;
 	    //hanki end
 	    
         Dimension = d;
     }
 
     public Grouper(int d, TFE t) {
     		//hanki start
 		order_flag = false;
 		aggregate_flag = false;
 	    //hanki end
 	    
         Dimension = d;
         tfe = t;
     }
 
     public void setId(int i) {
         id = i;
     }
 
     public int getId() {
         return id;
     }
 
     public void setTFE(TFE t) {
         tfe = t;
     }
 
     public void setDeco(DecorateList d) {
         decos = d;
     }
 
     public void addDeco(String key, Object val) {
         decos.put(key, val);
     }
 
     public void debugout() {
         debugout(0);
     }
 
     public void debugout(int count) {
 
         Debug dbgout = new Debug();
         dbgout.prt(count, "<Grouper type=" + getSymbol() + " decoitems="
                 + decos.size() + " id=" + id + ">");
 
         decos.debugout(count + 1);
         tfe.debugout(count + 1);
 
         dbgout.prt(count, "</Grouper>");
 
     }
 
     public ExtList makesch() {
 
         ExtList outsch = new ExtList();
         outsch.add(tfe.makesch());
         //  Log.out("Grp outsch:"+outsch);
 
         return outsch;
 
     }
 
     public ExtList makele0() {
 
         ExtList le0 = new ExtList();
 
         le0.add(this.getSymbol());
 
         le0.add(tfe.makele0());
 
         Log.out("Grp le0:" + le0);
 
         return le0;
 
     }
 
     public String getSymbol() {
         return "G?";
     }
 
     public void work(ExtList data_info) {
     }
 
     public int countconnectitem() {
         return 1;
     }
 
     private ExtList data;
 
     private int dindex;
 
     public void setDataList(ExtList d) {
         data = d;
         dindex = 0;
     }
 
     public boolean hasMoreItems() {
         return (dindex < data.size());
     }
 
     public void worknextItem() {
 
         ExtList subdata = (ExtList) (data.get(dindex));
         if (tfe instanceof Connector || tfe instanceof Attribute
                || tfe instanceof Function ) {
             tfe.work(subdata);
         } else {
             tfe.work((ExtList) subdata.get(0));
             Log.out(subdata.get(0));
         }
         dindex++;
 
     }
 
     public boolean isFirstItem() {
         return (dindex == 0);
     }
 	//hanki start
 	public void setOrderBy(String order) {
 		order_flag = true;
 
 		this.order = new String();
 		this.order = order;
 	}
 	
 	public void setAggregate(String aggregate) {
 		aggregate_flag = true;
 		
 		this.aggregate = new String();
 		this.aggregate = aggregate;
 	}
 	//hanki end
 	
 	//added by ria 20110913 start
 	public ExtList makeschImage() {
         ExtList outsch = new ExtList();
         outsch.add(tfe.makeschImage());
 
         return outsch;
 	}
 	//added by ria 20110913 end
 
 	public void addDeco(String name, String value, String condition) {
         decos.put(name, value, condition);
 		
 	}
 
 }
