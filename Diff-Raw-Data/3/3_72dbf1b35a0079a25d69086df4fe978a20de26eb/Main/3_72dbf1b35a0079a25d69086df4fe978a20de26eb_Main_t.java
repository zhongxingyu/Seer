 package labredes2;
 import java.util.*; 
 
 public class Main {
     static Node0 node0 = new Node0();
     static Node1 node1 = new Node1();
     static Node2 node2 = new Node2();
     static Node3 node3 = new Node3();
     static float clocktime;
     static int TRACE = 2;
     static int LINK_CHANGE = 10;
     static int FROM_LAYER2 = 2;
     static ArrayList<Event> evlist = new ArrayList<>();
     static Rtpkt mypktptr = new Rtpkt();
     static float lastime;
     static Event evptr = new Event();
      static int connectCosts[][] = new int[4][4];
      
     
     public static void main(String[] args) {
     
         Event eventptr;
         init();
         
         while (true){
             Iterator it = evlist.iterator();
             
             if (!it.hasNext()){
                 break;
             }
             eventptr = (Event) it.next();  /* get next event to simulate */
             
                         
             if(TRACE > 1){
                 System.out.print("MAIN: rcv event, t=" + eventptr.getEvtime() + ", at " + eventptr.getEventity()+"\n");
                 if(eventptr == null){
                     break;
                 }
                 if(eventptr.getEvtype() == FROM_LAYER2){
                     System.out.print(" src: " + eventptr.getRtpkt().getSourceid());
                     System.out.print(" dest: " + eventptr.getRtpkt().getDestid());
                     System.out.print(" contents: " + eventptr.getRtpkt().getMincost(0) +" "+
                                         eventptr.getRtpkt().getMincost(1) +" "+
                                         eventptr.getRtpkt().getMincost(2) +" "+
                                         eventptr.getRtpkt().getMincost(3));
                             
                 }
             }
             clocktime = eventptr.getEvtime();
             if (eventptr.getEvtype() == FROM_LAYER2){
                 if (eventptr.getEventity() == 0){
                     node0.rtupdate0(eventptr.getRtpkt());
                 }else if(eventptr.getEventity() == 1){
                     node1.rtupdate1(eventptr.getRtpkt());
                     //rtupdate1(eventptr.getRtpkt);                    
                 }else if(eventptr.getEventity() == 2){
                     node2.rtupdate2(eventptr.getRtpkt());
                     //rtupdate2(eventptr.getRtpkt);                    
                 }else if(eventptr.getEventity() == 3){
                     node3.rtupdate3(eventptr.getRtpkt());
                     //rtupdate3(eventptr.getRtpkt);                    
                 }else{
                     System.out.print("Panic: unknown event entity");
                 }
             }else if(eventptr.getEvtype() ==LINK_CHANGE){
                 if(clocktime < 10001.0){
                     //Tentar descobrir o que isso faz
                     //linkhandler0(1,20);
                     //linkhandler1(0,20);
                 }else{
                     //Aqui tbm
                     //linkhandler0(1,1);
                     //linkhandler1(0,1);
                 }
                 
             }else{
                 System.out.print("Panic: unknown event type");
             }
         }
         System.out.print("\nSimulator terminated at t=" + clocktime+ ", no packets in medium\n");
     }
     
     public static void init(){
         int i;
         long antes, depois;
         float sum, avg;
         float aux;
         Event evptr;
         Random rn = new Random(); //cria um número aleatório
         
         sum = 0;
         for (i=0; i<1000; i++){
 
             sum=sum + (rn.nextFloat() % 1);    /* jimsrand() should be uniform in [0,1] */
         }
         
         avg = sum/1000;
         
         if (avg < 0.25 || avg > 0.75) {
              System.out.printf("It is likely that random number generation on your machine\n" ); 
              System.out.printf("is different from what this emulator expects.  Please take\n");
              System.out.printf("a look at the routine jimsrand() in the emulator code. Sorry. \n");
          }   
         antes = System.currentTimeMillis();
         
         //inicializa os nós aqui
         node0.rtinit0();
         node1.rtinit1();
         node2.rtinit2();
         node3.rtinit3();
         
     }
     public static void toLayer2(Rtpkt pkt){
         
         System.out.print("Dentro do toLayer2 Origem: " + pkt.getSourceid() +" Destino: "+ pkt.getDestid()+"\n\n");
         int i;
         Random rn = new Random();
         //inicializando os custos das conexões
         connectCosts[0][0]=0;
         connectCosts[0][1]=1;  
         connectCosts[0][2]=3;
         connectCosts[0][3]=7;
         connectCosts[1][0]=1;  
         connectCosts[1][1]=0; 
         connectCosts[1][2]=1;
         connectCosts[1][3]=999;
         connectCosts[2][0]=3; 
         connectCosts[2][1]=1;  
         connectCosts[2][2]=0;
         connectCosts[2][3]=2;
         connectCosts[3][0]=7;  
         connectCosts[3][1]=999; 
         connectCosts[3][2]=2;
         connectCosts[3][3]=0;
     
         /* be nice: check if source and destination id's are reasonable */
         if (pkt.getSourceid() <0 || pkt.getSourceid() >3) {
           System.out.printf("WARNING: illegal source id in your packet, ignoring packet!\n");
           return;
           }
         if (pkt.getDestid() <0 || pkt.getDestid() >3) {
           System.out.printf("WARNING: illegal dest id in your packet, ignoring packet!\n");
           return;
           }
         if (pkt.getSourceid() == pkt.getDestid())  {
           System.out.printf("WARNING: source and destination id's the same, ignoring packet!\n");
           return;
           }
         if (connectCosts[pkt.getSourceid()][pkt.getDestid()] == 999)  {
           System.out.printf("WARNING: source and destination not connected, ignoring packet!\n");
           return;
           }
 
         /* make a copy of the packet student just gave me since he/she may decide */
         /* to do something with the packet after we return back to him/her */ 
         mypktptr.setSourceid(pkt.getSourceid());
         mypktptr.setDestid(pkt.getDestid());
          for (i=0; i<4; i++) {
             mypktptr.setMincost(i, pkt.getMincost(i));
         }
          if (TRACE > 2)  {
            System.out.printf("    TOLAYER2: source: "+mypktptr.getSourceid() + ", dest:"+mypktptr.getDestid() + " \n              costs:");
            for (i=0; i<4; i++) {
                  System.out.printf("\n" + mypktptr.getMincost(i));
            }
            System.out.printf("\n");
          }
         /* create future event for arrival of packet at the other side */
         evptr.setEvtype(FROM_LAYER2); /* packet will pop out from layer3 */
         evptr.setEventity(pkt.getDestid()); /* event occurs at other entity */
         evptr.setRtpkt(mypktptr);   /* save ptr to my copy of packet */
         
         /* finally, compute the arrival time of packet at the other end.
         medium can not reorder, so make sure packet arrives between 1 and 10
         time units after the latest arrival time of packets
         currently in the medium on their way to the destination */
         
         //lastime = clocktime;
         /*
         for (q = Main.evlist ; q != null; q = q.getNext()) {
             System.out.println("dentro desse for\n" + q.getNext() + "org: " + q);
             if ( (q.getEvtype()== FROM_LAYER2  && q.getEventity() == evptr.getEventity()) ) {
                 lastime = q.getEvtime();
             }
         }
         */
         evptr.setEvtime(lastime + 2 * (rn.nextFloat() % 1));// =  lastime + 2.*jimsrand();
 
 
         if (TRACE>2) {
             System.out.printf("    TOLAYER2: scheduling arrival on other side\n");
         }
         
         System.out.println("Sucesso");     
         evlist.add(evptr);
         
 
     }
 
 }
