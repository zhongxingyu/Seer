 package world;
 
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.ListIterator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 
 import sun.org.mozilla.javascript.ObjToIntMap.Iterator;
 
 
 public class Robot extends Elements {
 
   private Automate brain;
 
   private String name;
 
   private Equipe team ;
   
   private Bag sac;
   
   public Bag getSac() {
 	return sac;
 }
 
 
 public void setSac(Bag sac) {
 	this.sac = sac;
 }
 
 /*
  * a détruire
  */
 public void setteam(Equipe e) {
 	team = e ;
 }
 
 
 
 
 public Robot(Automate brain, Point p) {
 	  super(p);
 	  setBrain(brain);
 	  sac = new Bag(1) ;
   }
   
  //constructeur sans sac
   public Robot(Automate brain, Point p,String nom) {
 	  super(p);
 	  setBrain(brain);
 	  setName(nom);
 	  sac = new Bag(1) ;
   }
   
   public Robot(Automate brain, Point p,String nom,Bag sac){
 	  this(brain,p,nom);
 	  this.setSac(sac);
   }
   
   
   public void action(){
 	  String etat=getBrain().getCurrent().getName();
 	  if(etat=="goto"){
 		  //System.out.println("jai choisi goto") ;
 		  avancer();
 	  }
 	  if(etat=="ramasser") {
 		  //System.out.println("jai choisi ramasser") ;
 		  ramasser() ;
 		  //il faut changer l'objectif sinon check va aparaitre toujour!!!
 		  brain.getCurrent().setObjectif(new Point(10000,10000)) ;
 	  }
 	  if(etat == "back") {
 		  //System.out.println("jai choisi back") ;
 		  back() ;
 	  }
 	  if(etat == "init") { //System.out.println("jai choisi init") ;
 		  }
 	  }
   
 
   public void back() {
 	  //mettre à jour l'objectif avec la base la plus proche
 	  brain.getCurrent().setObjectif(team.getBase(this.getPosition())) ;
 	  avancer();
   }
   
   
   public void avancer(){
 	  Point oldPos = this.getPosition();
 	  PriorityQueue<DistanceTriees> L = new PriorityQueue<DistanceTriees>() ;
 	  Point courant ;
 	  Point objectif = brain.getCurrent().getObjectif() ; 
 	  int i,j ;//compteurs
 	  
 	  
 	  //Construction de la file à priorité
 	  //la priorité etant la plus petite distance entre le point et l'objectif
 	  
 	  //Creation de la liste à priorité
 	  for(i=oldPos.x-1 ; i<=oldPos.x+1 ; i++) {
 		  for(j=oldPos.y-1 ; j<=oldPos.y+1 ; j++) {
 			  courant = new Point(i,j) ;
 			  if(!(World.horsMap(courant)) && (World.caseLibre(courant) || World.getMap()[j][i] instanceof Balle)) {
 				  L.offer(new DistanceTriees(courant, objectif)) ;
 			  }
 		  }
 	  }
 	  
 	  //le bon point est en téte de la File!
 	  if(!L.isEmpty()) {
 		  setPosition(L.poll().getPoint()) ;
 		  World.modifier(this, oldPos) ;
 	  }
   }
   
   //en supposant que le robit ne ramasse qu'une seule balle à la fois
   public void ramasser(){
 	  this.getSac().ramasseNBalles(1);
 	  World.enleverBalle(this.getPosition()) ;
   }
   
   public void update(){
 	  LinkedList<Elements> resScan = World.scan(this);
 	  HashMap<String,Elements> resDecript = decript(resScan);
 	  brain.nextState(resDecript);
 	  action();
   }
   
   /*
    * Fonction qui retourne le string correspondant à la guard en fonction de l'élément
    * exemple : si l'élément c'est balle la fonction retourne "find"
    */
   private String elementToString(Elements e){
 	  if(e instanceof Robot )
 		  return "find";
 	  else 
 		  return "find";
   }
   
   
   public HashMap<String,Elements> decript(LinkedList<Elements> liste){
 	  HashMap<String,Elements> resultat = new HashMap<String, Elements>();
 	  ListIterator<Elements> it = liste.listIterator();
 	  Elements cour;
 	  
 	  if(this.getPosition().equals(brain.getCurrent().getObjectif())) {//a vérifier
 		  resultat.put("check",this) ;
 	  }
 	  
 	  while(it.hasNext()){
 		  cour = it.next();
 		  //resultat.put(elementToString(cour), cour);
 		  if(cour instanceof Balle) {
 			  resultat.put("find", cour) ; 
 		  }
 	  }
 	  
 	  if(resultat.isEmpty()) {resultat.put("ras",this) ;}
 	  return resultat;
   }
   
   public Automate getBrain() {
 	 return brain;
   }
 
    private void setBrain(Automate brain) {
 	 this.brain = brain;
   }
 
    public String getName() {
 	 return name;
   }
 
    private void setName(String name) {
 	this.name = name;
   }
 
 }
