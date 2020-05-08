 /*
  * Clase para manejar los graficos del juego principal, generar chunks,
  * mostrarlos, quitarlos ...
  */
 package bloques.graficos;
 
 import bloques.manejo.BloqueChunk;
 import bloques.manejo.BloqueChunkDatos;
 import bloques.manejo.BloqueChunkUtiles;
 import bloques.manejo.BloqueChunks;
 import bloques.manejo.BloqueGeneraTerreno;
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.input.InputManager;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.system.Timer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import jme3tools.optimize.GeometryBatchFactory;
 import utiles.Colision;
 
 /**
  *
  * @author mcarballo
  */
 public class BloqueGraficos{
     /**
      *
      */
     protected SimpleApplication app;
     /**
      *
      */
     protected Node              rootNode;
     /**
      *
      */
     protected AssetManager      assetManager;
     /**
      *
      */
     protected AppStateManager   stateManager;
     /**
      *
      */
     protected InputManager      inputManager;
     /**
      *
      */
     protected ViewPort          viewPort;
     /**
      *
      */
     protected BulletAppState    physics;
     /**
      *
      */
     protected Camera       cam;
     /**
      *
      */
     protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
     /**
      *
      */
     protected Future future = null;
     /**
      *
      */
     protected Future futureChunkUrgentes = null;
     
     /**
      * Objeto que contiene todo el procesado de posiciones y bloques
      */
     public BloqueGeneraTerreno bloqueGeneraTerreno;
     
     //clase para manejo de bloques
     BloqueGeneraBloque bloques;
         
     //graficos
     /**
      *
      */
     public BloqueChunks chunks;
     
     //aqui meteremos los updates del terreno nuevo que van sin prisa
     /**
      *
      */
     public Map<Integer,BloqueChunks> updates=new HashMap<Integer, BloqueChunks>();
     /**
      *
      */
     public int contadorUpdates = 0;
     
     //aqui meteremos los updates urgentes de cambios en los chunks, nuevos bloques ...
     /**
      *
      */
     public Map<Integer,String> updateChunkUrgentes=new HashMap<Integer, String>();
     /**
      *
      */
     public int contadorUpdatesChunkUrgentes = 0;
 
     
     /**
      * Constructor
      * @param app
      */
     public BloqueGraficos(Application app){
         this.app = (SimpleApplication) app;
         this.rootNode     = this.app.getRootNode();
         this.assetManager = this.app.getAssetManager();
         this.stateManager = this.app.getStateManager();
         this.inputManager = this.app.getInputManager();
         this.viewPort     = this.app.getViewPort();
         this.physics      = this.stateManager.getState(BulletAppState.class);
         this.cam          = this.app.getCamera();
         
         bloqueGeneraTerreno = new BloqueGeneraTerreno(app);
         
         bloques = new BloqueGeneraBloque(app);
         
         chunks = null;
     }
     
     /**
      *
      * @return
      */
     public Boolean generaTerrenoInicial(){
         bloqueGeneraTerreno.generaTerreno();
             
         //ya termino de generar el terreno
         if (!bloqueGeneraTerreno.generandoTerreno){
             chunks = bloqueGeneraTerreno.getChunks();
             
             //tenemos que pasar a updates estos chunks
             for (int x = 0;x<bloqueGeneraTerreno.totalTamano;x = x + BloqueChunkUtiles.TAMANO_CHUNK){
                 for (int z = 0;z<bloqueGeneraTerreno.totalTamano;z = z + BloqueChunkUtiles.TAMANO_CHUNK){
                     BloqueChunks grupoChunks = chunks.getGrupoChunks(x * BloqueChunkUtiles.TAMANO_BLOQUE, z * BloqueChunkUtiles.TAMANO_BLOQUE);
                     updates.put(contadorUpdates,grupoChunks);
                     contadorUpdates++;
                 }
             }
 
             bloqueGeneraTerreno.vaciaChunks();
             
             return true;
         }
         
         return false;
     }
     
     /**
      *
      * @param updatear 
      * @param tipoUpdate con esta variable controlamos los urgentes de los que no lo son
      * @throws InterruptedException
      * @throws ExecutionException
      */
     @SuppressWarnings("SleepWhileInLoop")
     public void updateaChunks(Map<Integer,BloqueChunks> updatear, int tipoUpdate) throws InterruptedException, ExecutionException{
         Timer timer = app.getTimer();
         float totalInicio = timer.getTimeInSeconds();
 
         //recorremos el array
         Integer[] keys = (Integer[])( updatear.keySet().toArray( new Integer[updatear.size()] ) ); 
         
         if (keys.length > 0){
             for(int i=0; i<keys.length; i++){
                 int claveActual = keys[i];
                 
                 BloqueChunks updatando = (BloqueChunks) updatear.get(claveActual);
                 
                 //sacamos todos los chunks a updatar y lo recorremos chunk a chunk
                 Map<String, BloqueChunk> allChunks = updatando.getAllChunks();
                 String[] keysAllChunks = (String[])( allChunks.keySet().toArray( new String[allChunks.size()] ) );
                 if (keysAllChunks.length > 0){
                     for(int j=0; j<keysAllChunks.length; j++){
                         String claveActualAllChunks = keysAllChunks[j];
                         
                         float inicio = timer.getTimeInSeconds();
                         
                         BloqueChunk chunkActual = allChunks.get(claveActualAllChunks);
   
                         int tamano = BloqueChunkUtiles.TAMANO_CHUNK;
 
                         Node bloquesMostrar = new Node("Chunk: "+claveActualAllChunks);
 
                         //System.out.println("chunk "+claveActualAllChunks);
 
                         int mostrar = 0;
 
                         for(int x = 0;x<tamano;x++){
                             for(int y = 0;y<tamano;y++){
                                 for(int z = 0;z<tamano;z++){
                                     BloqueChunkDatos datosBloque = chunkActual.getDatosBloque(x, y, z);
 
                                     if (datosBloque != null){
                                         Node bloqueClonado;
                                         bloqueClonado = bloques.getBloqueGenerado(datosBloque.getNomBloque());
                                         
                                         //coordenadas reales del cubo, no las del chunk
                                         final int[] coordenadas = BloqueChunkUtiles.calculaCoordenadasBloqueAPartirDeChunk(claveActualAllChunks, x * BloqueChunkUtiles.TAMANO_BLOQUE, y  * BloqueChunkUtiles.TAMANO_BLOQUE, z * BloqueChunkUtiles.TAMANO_BLOQUE);
 
                                         //le quitamos las caras que no se ven
                                         int[] carasbloquesVecinos ;
                                         int contaCarasQuitadas = 0;
                                         if (tipoUpdate == 1){
                                             int[][] bloquesVecinos = chunks.getBloquesVecinos(coordenadas[0],coordenadas[1],coordenadas[2]);
                                             carasbloquesVecinos = chunks.getCarasAPartirDeBloquesVecinos(bloquesVecinos);
                                             datosBloque.setCaras(carasbloquesVecinos);
                                         }else{
                                             carasbloquesVecinos = datosBloque.getCaras();
                                         }
                                         
                                         for(int h = 0;h<6;h++){
                                             if (carasbloquesVecinos[h] == 0){ //si no hay cara
                                                 bloqueClonado.detachChildNamed("Cara-"+h); 
                                                 contaCarasQuitadas++;
                                             }
                                         }
 
                                         if (contaCarasQuitadas < 6){
                                             bloqueClonado.move(coordenadas[0],coordenadas[1],coordenadas[2] + BloqueChunkUtiles.TAMANO_BLOQUE);
                                             
                                             //TangentBinormalGenerator.generate(bloqueClonado);
                                             //bloqueClonado.setShadowMode(ShadowMode.CastAndReceive);
                                             
                                             bloquesMostrar.attachChild(bloqueClonado);
 
                                             mostrar = 1;
                                         }
                                     }
                                 }
                             }
                         }
                         
                         float fin = timer.getTimeInSeconds();
                         
                         System.out.println("Tiempo chunk "+claveActualAllChunks+" "+(fin-inicio));
                         //System.out.println("chunk "+claveActualAllChunks+" Terminado");
 
                         if (mostrar == 1){
                             final Spatial optimizado = GeometryBatchFactory.optimize(bloquesMostrar);
                             final int tipoUpdateFinal = tipoUpdate;
                             final String claveActualAllChunksFinal = claveActualAllChunks;
 
                             app.enqueue(new Callable() {
                                 public Object call() throws Exception {          
                                     CollisionShape bloquesMostrarShape = CollisionShapeFactory.createMeshShape((Node) optimizado);
                                     RigidBodyControl bloquesMostrarControl = new RigidBodyControl(bloquesMostrarShape, 0);
                                     optimizado.addControl(bloquesMostrarControl);
                             
                                     physics.getPhysicsSpace().add(optimizado);
                                                                         
                                     rootNode.attachChild(optimizado);
                                     
                                     //quitamos el chunk anterior y sus fisicas si hace falta
                                     if (tipoUpdateFinal == 2){
                                         Spatial child = rootNode.getChild("Chunk: "+claveActualAllChunksFinal);
                                         physics.getPhysicsSpace().remove(child.getControl(0)) ; 
                                         rootNode.detachChild(child);
                                     }
                                     
                                     //TangentBinormalGenerator.generate(optimizado);
                                     //optimizado.setShadowMode(ShadowMode.CastAndReceive);
                                                                         
                                     return null;
                                 }
                             });
 
                         }
                     }
                 }
                 //Thread.sleep(10);
             }
         }
         
         float totalFin = timer.getTimeInSeconds();
         //System.out.println("Tiempo update"+(totalFin-totalInicio));
         
     }
     
     // A self-contained time-intensive task:
     private Callable<Boolean> procesaGraficosUpdates = new Callable<Boolean>(){
         public Boolean call() throws Exception {
             Map<Integer,BloqueChunks> updatear = app.enqueue(new Callable<Map<Integer,BloqueChunks>>() {
                 public Map<Integer,BloqueChunks> call() throws Exception {
                     return getUpdates(true);
                 }
             }).get();
             
             if (updatear != null){
                 updateaChunks(updatear,1); //1 es para los updates lentos del terreno
             }
             
             return false;
         }
     };
     
     // A self-contained time-intensive task:
     private Callable<Boolean> procesaGraficosUpdatesUrgentes = new Callable<Boolean>(){
         public Boolean call() throws Exception {
             Map<Integer,BloqueChunks> updatear = app.enqueue(new Callable<Map<Integer,BloqueChunks>>() {
                 public Map<Integer,BloqueChunks> call() throws Exception {
                     return getUpdatesUrgentes(true);
                 }
             }).get();
             
             if (updatear != null){
                 updateaChunks(updatear, 2); //2 es para los rapidos, destruir bloques por ejemplo
             }
             
             return false;
         }
     };
     
     /**
      * Esta funcion se llamara en el update del AppState
      * @param tpf 
      */
     public void update(float tpf){  
         //las siguientes veces que se hace update se actualizan los chunks
        try{
             if(future == null){
                 future = executor.submit(procesaGraficosUpdates);
             }
             else if(future != null){
                 if(future.isDone()){
                     future = null;
                 }
                 else if(future.isCancelled()){
                     future = null;
                 }
             }
         } 
         catch(Exception e){ 
 
         }
 
         //updatemos los chunks urgentes
         try{
             if(futureChunkUrgentes == null){
                 futureChunkUrgentes = executor.submit(procesaGraficosUpdatesUrgentes);
             }
             else if(futureChunkUrgentes != null){
                 if(futureChunkUrgentes.isDone()){
                     futureChunkUrgentes = null;
                 }
                 else if(futureChunkUrgentes.isCancelled()){
                     futureChunkUrgentes = null;
                 }
             }
         } 
         catch(Exception e){ 
 
         }
     }
     
     /**
      *
      * @param vaciarUpdate
      * @return
      */
     public Map<Integer,BloqueChunks> getUpdates(Boolean vaciarUpdate){
         if (vaciarUpdate){
             if (updates == null){
                 return null;
             }
             
             Map<Integer,BloqueChunks> updatesCopia = new HashMap<Integer, BloqueChunks>();
 
             Boolean hayDatos = false;
             
             SortedSet<Integer> keys = new TreeSet<Integer>(updates.keySet());
             for (Integer key : keys) { 
                 updatesCopia.put(key,updates.get(key));
                 updates.remove(key);
                 hayDatos = true;
                 break;
             }
             
             if (hayDatos){
                 return updatesCopia;
             }else{
                return null;
             }
         }else{
             //quiza se use mas adelante
             return null;
         }  
     }
     
     /**
      *
      * @param vaciarUpdate
      * @return
      */
     public Map<Integer,BloqueChunks> getUpdatesUrgentes(Boolean vaciarUpdate){
         if (vaciarUpdate){
             if (updateChunkUrgentes == null){
                 return null;
             }
             
             Map<Integer,BloqueChunks> updatesCopia = new HashMap<Integer, BloqueChunks>();
             
             BloqueChunks chunksCopia = new BloqueChunks();
 
             Boolean hayDatos = false;
             
             SortedSet<Integer> keys = new TreeSet<Integer>(updateChunkUrgentes.keySet());
             for (Integer key : keys) { 
                 int[] calculaCoordenadasChunkAPartirDeNombreChunk = BloqueChunkUtiles.calculaCoordenadasChunkAPartirDeNombreChunk(updateChunkUrgentes.get(key));
                 
                 chunksCopia.setChunk(calculaCoordenadasChunkAPartirDeNombreChunk[0], calculaCoordenadasChunkAPartirDeNombreChunk[1], calculaCoordenadasChunkAPartirDeNombreChunk[2], 
                            chunks.getChunk(calculaCoordenadasChunkAPartirDeNombreChunk[0], calculaCoordenadasChunkAPartirDeNombreChunk[1], calculaCoordenadasChunkAPartirDeNombreChunk[2]));                
                 
                 updateChunkUrgentes.remove(key);
                 hayDatos = true;
             }
             
             if (hayDatos){
                 updatesCopia.put(contadorUpdatesChunkUrgentes,chunksCopia);
                 contadorUpdatesChunkUrgentes++;
                 
                 return updatesCopia;
             }else{
                return null;
             }
         }else{
             //quiza se use mas adelante
             return null;
         }
     }
     
     /**
      *
      * @param accion
      * @param nomBloque
      * @param coordUsar
     * @param posicionPlayer
      */
    public void accionBloque(String accion, String nomBloque, int[] coordUsar, Vector3f posicionPlayer){
         Boolean bloqueAccionado = false;
         
         if (accion.equals("destruir")){
             bloqueAccionado = chunks.destruyeBloque(coordUsar[0], coordUsar[1], coordUsar[2]);
         }else if(accion.equals("colocar")){
            //tenemos que comprobar si el player no estamos en el mismo lugar que el bloque a colocar
            if (posicionPlayer != null && !Colision.calculaColisionPlayer(coordUsar, posicionPlayer)){
                 bloqueAccionado = chunks.colocaBloque(coordUsar[0], coordUsar[1], coordUsar[2], nomBloque);
             }
         }
         
         if (bloqueAccionado){
                 Map<String,Integer> chunksAUpdatar=new HashMap<String,Integer>();      
                 String nombreChunk;
                 
                 //recargamos el chunk donde esta el bloque de la colision
                 nombreChunk = BloqueChunkUtiles.generarNombreChunk(BloqueChunkUtiles.calculaCoordenadasChunk(coordUsar[0], coordUsar[1], coordUsar[2]));
                 if (chunksAUpdatar.get(nombreChunk) == null){
                     chunksAUpdatar.put(nombreChunk,1);
                 }
                 
                 if (accion.equals("colocar")){
                     //calculamos sus caras
                     chunks.setCarasVecinas(coordUsar[0], coordUsar[1], coordUsar[2]);
                 }
 
                 int[][] bloquesVecinos = chunks.getBloquesVecinos(coordUsar[0], coordUsar[1], coordUsar[2]);
                 for(int i=0;i<6;i++) {
                     if (bloquesVecinos[i] != null){ //si hay bloque vecino
                         //calculamos sus caras
                         chunks.setCarasVecinas(bloquesVecinos[i][0], bloquesVecinos[i][1], bloquesVecinos[i][2]);
 
                         nombreChunk = BloqueChunkUtiles.generarNombreChunk(BloqueChunkUtiles.calculaCoordenadasChunk(bloquesVecinos[i][0], bloquesVecinos[i][1], bloquesVecinos[i][2]));
                         if (chunksAUpdatar.get(nombreChunk) == null){
                             chunksAUpdatar.put(nombreChunk,1);
                         }  
                     }
                 }
 
                 String[] keysChunksUpdatar = (String[])( chunksAUpdatar.keySet().toArray( new String[chunksAUpdatar.size()] ) );
                 if (keysChunksUpdatar.length > 0){
                     for(int j=0; j<keysChunksUpdatar.length; j++){
                         String claveActual = keysChunksUpdatar[j];
                         updateChunkUrgentes.put(contadorUpdatesChunkUrgentes, claveActual);
                         contadorUpdatesChunkUrgentes++;
                     }
                 }
             }
     }
     
     /**
      *
      */
     public void destroy() {
         bloqueGeneraTerreno.destroy(); //lo ejecutamos para cerrar los hilos que pueda haber abiertos
         executor.shutdown();
         executor.shutdownNow();
     }
 }
