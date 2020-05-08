 public class List
 {
 	private static PCObject _obj = new PCObject();
 
 	static {
 
 		_obj.set("length", new IPCFunction(){
 			@Override
 			public PCObject call(PCObject... args){
 				PCList list = (PCList)args[0];
 				return new PCObject(list.size());
 
 			}
 		});
 
 		_obj.set("add", new IPCFunction(){
 			@Override
 			public PCObject call(PCObject... args){
 				PCList list = (PCList)args[0];
 				list.add(args[1]);
 				return list;
 			}
 		});
 
 
 		_obj.set("remove", new IPCFunction(){
 			@Override
 			public PCObject call(PCObject... args){
 				PCList list = (PCList)args[0];
 				int idx = args[1].<Integer>getBase();
 				list.removeAt(idx);
 				return list;
 			}
 		});
 
 		_obj.set("map", new IPCFunction() {
 			@Override
 			public PCObject call(PCObject... args) {
 				PCList list = (PCList)args[0];
 				IPCFunction mapper = (IPCFunction)args[1];
 				PCList newList = new PCList();
 				for (PCObject o : list) {
 					newList.add(mapper.call(o));
 				}
 				return newList;
 			}
 		});
 
 		_obj.set("where", new IPCFunction() {
 			@Override
 			public PCObject call(PCObject... args) {
 				PCList list = (PCList)args[0];
 				IPCFunction filter = (IPCFunction)args[1];
 				PCList newList = new PCList();
 				for (PCObject o : list) {
 					if (filter.call(o).<Boolean>getBase()) {
 						newList.add(o);
 					}
 				}
 				return newList;
 			}
 		});
 
 		_obj.set("find", new IPCFunction(){
 			@Override
 			//adapted from algs4.cs.princeton.edu/53substrings
 			public PCObject call(PCObject... args){
 				PCList pat = (PCList)args[1];
 				PCList txt = (PCList)args[0];
 
 				int M = pat.size();
         		int N = txt.size();
 
 		        for (int i = 0; i <= N - M; i++) {
 		            int j;
 		            for (j = 0; j < M; j++) {
		                if (txt.<PCObject>get(i+j).equals(pat.<PCObject>get(j)))
 		                    break;
 		            }
 		            if (j == M) return new PCObject(i);            // found at offset i
 		        }
 		        return new PCObject(-1)	;                            // not found
 
 			}
 		});
 
 		_obj.set("split", new IPCFunction(){
 			@Override
 			public PCObject call(PCObject... args){
 				PCList list = (PCList)args[0];
 				PCObject wedge = args[1];
 				PCList output = new PCList();
 				int lBound = 0;
 				int uBound = 0;
 
 				for(PCObject element : list){
 					if(element.equals(wedge)){
 						output.add(list.subList(lBound,uBound));
 						lBound = uBound+1;
 					}
 					uBound++;
 				}
 				return output;
 
 			}
 		});
 
 		_obj.set("range", new IPCFunction(){
 			@Override
 			public PCObject call(PCObject... args){
 				PCList output = new PCList();
 				int lLimit = args[0].<Integer>getBase();
 				int uLimit = args[1].<Integer>getBase();
 				for(;lLimit<=uLimit;lLimit ++){
 					output.add(new PCObject(lLimit));
 				}
 				return output;
 			}
 
 		});
 
 
 	}
 
     public static <T> T get(String key) {
     	return _obj.<T>get(key);
     }
 }
