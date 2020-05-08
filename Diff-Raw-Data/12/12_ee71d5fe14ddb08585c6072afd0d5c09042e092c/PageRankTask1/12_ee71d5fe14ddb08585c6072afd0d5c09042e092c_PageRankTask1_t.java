 package assignment;
 
 import java.util.Random;
 
 public class PageRankTask1 {
 
 	/* Utilisez cet objet pour générer des nombres aléatoires*/
 	public static Random random = new Random(2013);     
 
 	public static void main(String[] argv) {   
 		/*Réseau de pages exemple*/
 		int [][] net = {
 			{ 1, 2 },    //page 0
 			{ 2, 2, 4},  //page 1
 			{ 4 },       //page 2
 			{ 0, 0},     //page 3
 			{ 1, 2 , 4}  //page 4
 		};
 		
 		int[] path = randomSurfer(net, 10);
 		for (int i = 0; i < path.length; i++) {
 			System.out.println("L'utilisateur visite la page " + path[i]);
 		}
 		System.out.println("Visualisation graphique des visites : ");
 		for (int i = 0; i < path.length; i++) {
 			System.out.println(visualizeVisit(path[i], net.length));
 		}
 	}
 
 	public static int[] randomSurfer(int[][] net, int steps) {
 		
		int[] path = new int[steps+1];
		path[0]=0;
 		int surfer = 0;
 		
		for(int i=1 ; i<=steps ; i++)
 			surfer = path[i] = getNextPage(net, surfer);
 		
 		return path;
 	}
 	
 	public static int getNextPage(int[][] net, int currentPage) {
		if (net[currentPage].length==0 || random.nextDouble()<0.1)
 			return random.nextInt(net.length);
 		return net[currentPage][random.nextInt(net[currentPage].length)];
 	}
 
 	public static String visualizeVisit(int page, int totalPageNum) {
 		/* Méthode à coder */
		return "-----";
 	}
 }
