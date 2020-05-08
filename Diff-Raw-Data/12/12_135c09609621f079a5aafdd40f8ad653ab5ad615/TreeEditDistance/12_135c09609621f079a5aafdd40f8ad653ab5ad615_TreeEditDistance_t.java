 package domted;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import org.w3c.dom.Node;
 
 public class TreeEditDistance {
 	NodeDistance nd;
 	public TreeEditDistance(NodeDistance nd) {
 		this.nd = nd;
 	}
 	public int calculate(Node n1,Node n2) {
 		NumberFormat formatter = new DecimalFormat("#00");
 		DOMPostOrder t1 = new DOMPostOrder(n1);
 		DOMPostOrder t2 = new DOMPostOrder(n2);
 		int[][] td = new int[t1.N.length][t2.N.length];
 		int[][] fd = new int[t1.N.length][t2.N.length];
 		for(int s=0;s<t1.kr.length;s++) {
 			for(int t=0;t<t2.kr.length;t++) {
 				forestDist(t1.kr[s],t2.kr[t],t1.L,t2.L,t1.N,t2.N,td,fd);
 			}
 		}
 		/*
 		for(int i=0;i<td.length;i++) {
 			for(int j=0;j<td[i].length;j++) {
 				System.out.print(" "+formatter.format(td[i][j]));
 			}
 			System.out.println();
 		}*/
 		return td[td.length-1][td[0].length-1];
 	}
 	
 	private void forestDist(int i, 		int j,
 							Integer[] li,	Integer[] lj,
 							Node[] nodes1,Node[] nodes2,
 							int[][] td,int[][] fd) {
		fd[li[i]-1][lj[j]-1] = 0;
 		for(int di=li[i];di<=i;di++) fd[di][lj[j]-1] = fd[di-1][lj[j]-1]+nd.delete(nodes1[di], nodes2[lj[j]-1]);
 		for(int dj=lj[j];dj<=j;dj++) 
 			fd[li[i]-1][dj] = 
 				fd[li[i]-1][dj-1] 
 			+ nd.insert(
 					nodes1[li[i]-1],
 					nodes2[dj]);
 		for(int di=li[i];di<=i;di++) {
 			for(int dj=lj[j];dj<=j;dj++) {
 				if(li[di]==li[i] && lj[dj]==lj[j]) {
 					fd[di][dj] =
 					Math.min(		fd[di-1][dj] + nd.delete(nodes1[di], nodes2[dj]),
 						Math.min(	fd[di][dj-1] + nd.insert(nodes1[di], nodes2[dj]),
 									fd[di-1][dj-1] + nd.rename(nodes1[di], nodes2[dj])
 								)
 					);
 					td[di][dj]=fd[di][dj];
 				} else 	fd[di][dj] =
 					Math.min(		fd[di-1][dj] + nd.delete(nodes1[di], nodes2[dj]),
 						Math.min(	fd[di][dj-1] + nd.insert(nodes1[di], nodes2[dj]),
 									fd[li[di]-1][lj[dj]-1] + td[di][dj]
 							)
 					);
 			}
 		}
 		
 		
 	}
 	
 
 }
