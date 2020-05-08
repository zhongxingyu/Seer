 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphy;
 
 import java.util.*;
 /**
  *
  * @author Glebuz
  */
 
     class Rebro 
     {
         public int Start;
         public int Finish;
         public int Weight;
         
         public Rebro (int a, int b, int c)
         {
             Start = a;
             Finish = b;
             Weight = c;
         }
     }
     
 public class Graphy {   
     
     private static Random random = new Random();    
     public int count, numberNode;
     private int [] [] matrixSmezhn ;
     public int [] nodeCount = null;
     
     public static Graphy gr = new Graphy ();
         
     public static int generate ()// генератор случайных чисел            
     {
         return Math.abs(random.nextInt() % 10);
     }
     
     public String CreateGraph ()// создаем дерево p.s. необходимо ввести кол-во
     {
         System.out.println("Enter the number of nodes: ");
         Scanner in = new Scanner(System.in);
         count = in.nextInt();   
         System.out.println("You entered " + count);
         return "-----------------------------------";
     }
     public String CreateMatrix ()// создаем матрицу смежности
     {
         matrixSmezhn = new int [count] [count];
         for (int i = 0; i < count; i++ )
         {        
             for (int j = i+1; j < count; j++)
             {
                 matrixSmezhn [i][j] = generate();
                 matrixSmezhn [j][i] = matrixSmezhn [i][j];                
             }  
         }
         System.out.println("The adjacency matrix is: ");
         for (int i = 0; i < count; i++)
         {
             for (int j = 0; j < count; j++)
             {
                 System.out.print(matrixSmezhn [i][j] + " ");
             }
             System.out.println();
         }
         return "Creating of adjacency matrix. Ok.";
     }
     public String Obhod ()
     {
         System.out.println("-----------------------------------");
         Stack <Integer> st = new Stack <Integer>();//стек обхода          
         List <Integer> poryadokObhoda = new LinkedList <Integer> ();// список порядка обхода
         boolean [] used = new boolean [count];
         st.push(0);// пихаем в стек первую вершину
         used [0] = true;        
         while(!st.empty() || isAllUsed(st,used))
         {
             int f = st.pop();           
             poryadokObhoda.add(f);
             for (int i = 0; i < count; i++)
             {
                 if (matrixSmezhn[f][i] > 0 && !used[i])
                 {
                     st.push(i);
                     used [i] = true;
                 }
             }
         }
         System.out.println(poryadokObhoda);
         return "Depth-first search is done.";
     }
     
     public String Prima ()
     {
         System.out.println("-----------------------------------");
         List <Rebro> stBor = new LinkedList <Rebro> ();// стек обхода
         List <String> minOstDer = new LinkedList <String> ();// список минимального остовного дерева
         boolean [] usedNodes = new boolean [count];
         int staNum, finNum = 0, finNumOfNext, weiNum, minWay, maxWay,nextWay = 0;
         boolean countWays = false;
         maxWay = 0;
         minWay = 0;
         for (int i = 0; i < count; i++)
         {
             for (int j = 0; j < count; j++)
             {
                 if (matrixSmezhn[i][j] > maxWay) maxWay = matrixSmezhn[i][j];// находим макс путь
             }
         }
 ////////////////////////////////////////////////////////////////////////////////        
         staNum = 0;
         finNumOfNext = 0;
         minWay = maxWay;
         
         boolean contin = true; // продолжать = true
 ////////////////////////////////////////////////////////////////////////////////     
         while (contin == true)// до тех пор, пока продолжать = true
         {
             usedNodes [finNumOfNext] = true;// помечаем исходный как использованный
             minWay = maxWay;// делаем минимальный путь максимальным
             staNum = finNumOfNext;
             for (int i = 0; i < count; i++)// ищем мин путь дальше
             {                    
                 if (!usedNodes[i] && matrixSmezhn[staNum][i] > 0 && matrixSmezhn[staNum][i] <= minWay)
                     // если вершина не использовалась и путь меньше минимального и больше нуля 
                 {
                     minWay = matrixSmezhn[staNum][i];//мин путь == путь текущего ребра
                     finNumOfNext = i;// конечный узел = i
                     countWays = true;// найден путь = 1
                 }
 
             }
             if (countWays == false)// если новых путей не найдено
             {
                 int minWei = maxWay;
                 int nmb = 0;
                 for (int k = stBor.size(); k > 0; k--)// проверяем список обхода с конца
                 {
                     Rebro rebro1 =  stBor.get(k - 1);
                     if ( !usedNodes[rebro1.Finish] && rebro1.Weight <= minWei)
                     {
                     staNum = rebro1.Start;
                     finNumOfNext = rebro1.Finish;
                     nmb = k - 1;
                     }
                     
                 }
                 stBor.remove(nmb);
             }
             countWays = false;
             minOstDer.add(Integer.toString(staNum) + "-" + Integer.toString(finNumOfNext));// добавляем ребро в мин ост дер
             usedNodes[staNum] = true;                 
 
             for (int i = 0; i < count; i++)
             {
                 if (matrixSmezhn[staNum][i] > 0 && !usedNodes[i])
                     // если есть путь и узел не использовался, добавляем в стек обхода
                 {
                     nextWay = matrixSmezhn[staNum][i];// вес следующего пути
                     finNum = i; // конечный узел = i
                     Rebro rebro = new Rebro (staNum,finNum,nextWay);
                     stBor.add(rebro);
                 }
             }
             contin = false;
             int i = 0;
             while (i < count)
             {   
                 if(usedNodes[i] == false)
                 {
                     contin = true;
                     break;
                 }
                 i++;
             }            
         }
             for (int i = 0; i < minOstDer.size() - 1; i++)
             {
             System.out.print(minOstDer.get(i) + "; ");
             }
         System.out.println();
         return "Prim's algorithm is done.";
     }
     
     public String Boruvki ()
     {
         
         System.out.println("-----------------------------------");
         int [] usedNodes = new int [count];// массив пометок использованности вершин
         for (int i = 0; i < count; i++)
         {
             usedNodes [i] = i + 1;
         }
         int maxRebro = -1;
         // найдем максимальное ребро
         for (int i = 0; i < count; i++)
         {
             for (int j = 0; j < count; j++)
             {
                 if (matrixSmezhn[i][j] > maxRebro) maxRebro = matrixSmezhn[i][j] + 1;// находим макс ребро
             }
         }        
         // цикл
         boolean contin = true;
         int minVersh1 = -1, minVersh2 = -1;
         while (contin == true)
         {
             contin = false;
             int minRebro = maxRebro;
             for (int i = 0; i < count; i++)
             {
                 for (int j = i + 1; j < count; j++)
                 {
                     if((matrixSmezhn[i][j] < minRebro) && (matrixSmezhn[i][j] > 0) && (usedNodes[i] != usedNodes[j]))
                         // если путь меньше минпути и путь существует и это не путь в самого себя
                     {                        
                         minRebro = matrixSmezhn[i][j];//обновляем 
                         minVersh1 = i;// запоминаем первую вершину
                         minVersh2 = j;// запоминаем вторую вершину
                         contin = true;// имеет смысл продолжать
                     }
                 }
             }
             if (contin == true)
             {
                 matrixSmezhn[minVersh1][minVersh2] = 0;
                 int numbComp = usedNodes[minVersh1];//запоминаем номер компоненты
                 int changedComp = usedNodes[minVersh2];// запоминаем номер компоненты
                 if (usedNodes[minVersh1] > usedNodes[minVersh2])// если если номер компоненты 1 > 2 меняем их местами
                 {
                     numbComp = usedNodes[minVersh2];
                     changedComp = usedNodes[minVersh1];
                 }
                 for (int i = 0; i < count; i++)
                 {
                     if (usedNodes[i] == changedComp) usedNodes[i] = numbComp;//переписываем все 2-ые компоненты под первые
                 }
                 contin = false;// перед проверкой, что все вершины не принадлежат елинственной компоненте
                 for (int i = 0; i < count; i++)
                 {
                     if (usedNodes[i] > 1)// если какой либо узел больше чем 1
                     {
                         contin = true;// имеет смысл продолжать
                         break;
                     }
                 }
                 System.out.print(minVersh1);// вывод ребра              
                 System.out.print("-");              
                 System.out.print(minVersh2);
                 System.out.print("; ");
                 
             }
             
         }
         System.out.println();
         return "Boruvka's algorithm is done";
     }
 
  public static void main(String args[]) 
  { 
      System.out.println(gr.CreateGraph());
      System.out.println(gr.CreateMatrix());
      System.out.println(gr.Obhod());
      System.out.println(gr.Prima());
      System.out.println(gr.Boruvki());
  }
 
     private boolean isAllUsed(Stack st, boolean [] used) 
     {
         for (int i = 0; i < used.length; i++)
         {
             if (!used [i]) 
             {
                 st.push(i); 
                 used[i] = true;
                 return true;
             }
             
         }
         return false;
     }
 }
