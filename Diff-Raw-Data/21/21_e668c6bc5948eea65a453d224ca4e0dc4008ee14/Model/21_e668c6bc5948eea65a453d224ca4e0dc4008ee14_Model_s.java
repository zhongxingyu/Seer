 package gx.realtime;
 
 import gx.util.RandomUtils;
 
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class Model extends EventTarget {
 
     private Document document;
     private CollaborativeMap<CollaborativeObject> root;
     private boolean initialized;
     private LinkedList<BaseModelEvent> undoableMutations;
     private LinkedList<BaseModelEvent> redoableMutations;
     private boolean readOnly;
 
 
     protected Model(Document document){
         this.document = document;
         this.root = new CollaborativeMap<CollaborativeObject>(RandomUtils.getRandomAlphaNumeric(), this);
         initialized = true;
         undoableMutations = new LinkedList<BaseModelEvent>();
         redoableMutations = new LinkedList<BaseModelEvent>();
         readOnly = false;
 
         addDocumentEventHandlers();
     }
     
     private void addDocumentEventHandlers() {
         document.addEventListener(EventType.VALUES_ADDED, (ValuesAddedEvent e) -> {
             System.out.println("VALUES_ADDED");
         });
     }
     
     public void beginCompoundOperation(String opt_name){
         //TODO
     }
     
     public void beginCompoundOperation(){
         //TODO
     }
     
     public Object create(/*TODO*/){
         //TODO
         return null;
     }
     
    public <E extends CollaborativeObject> CollaborativeList<E> createList(){
         return new CollaborativeList<E>(RandomUtils.getRandomAlphaNumeric(), this);
     }
     
    public <E extends CollaborativeObject> CollaborativeList<E> createList(E[] opt_initialValue){
         CollaborativeList<E> result = createList();
         result.pushAll(opt_initialValue);
         return result;
     }
     
    public <E extends CollaborativeObject> CollaborativeMap<E> createMap(){
         return new CollaborativeMap<E>(RandomUtils.getRandomAlphaNumeric(), this);
     }
     
    public <E extends CollaborativeObject> CollaborativeMap<E> createMap(Map<String, E> opt_initialValue){
         CollaborativeMap<E> result = createMap();
         Set<Entry<String, E>> entries = opt_initialValue.entrySet();
         for(Entry<String, E> entry : entries){
             result.set(entry.getKey(), entry.getValue());
         }
         return result;
     }
     
     public CollaborativeString createString(){
         return new CollaborativeString(RandomUtils.getRandomAlphaNumeric(), this);
     }
     
     public CollaborativeString createString(String opt_initialValue){
         CollaborativeString result = createString();
         result.setText(opt_initialValue);
         return result;
     }
 
     public void endCompoundOperation(){
         //TODO
     }
     
     public CollaborativeMap<CollaborativeObject> getRoot(){
         //TODO: make sure that the documents in this root element are updated
         return root;
     }
     
     public boolean isInitialized(){
         return initialized;
     }
 
     //TODO: maybe make this public in order to call it from the node that was actually mutated?
     private void registerMutation(BaseModelEvent e){
         undoableMutations.push(e);
     }
 
     public void redo(){
         if(this.canRedo()){
             //TODO: redo last action of redoableMutation stack.
         }
     }
     
     public void undo(){
         if(this.canUndo()){
             //TODO: undo last action of undoableMutation stack.
         }
     }
     
     public boolean canRedo(){
         return redoableMutations.size() > 0;
     }
     
     public boolean canUndo(){
         return undoableMutations.size() > 0;
     }
     
     public boolean isReadOnly(){
         return readOnly;
     }
 
     @Override
     public void fireEvent(BaseModelEvent event, BubbleCallback callback) {
 
         BubbleCallback topCallback = () ->{
             super.fireEvent(event, null);
             callback.excecute();
         };
         root.fireEvent(event, callback);
 
         //TODO: registerMutation. Only if event has actually changed an object?
         //TODO: clear redoable stack?
     }
 }
