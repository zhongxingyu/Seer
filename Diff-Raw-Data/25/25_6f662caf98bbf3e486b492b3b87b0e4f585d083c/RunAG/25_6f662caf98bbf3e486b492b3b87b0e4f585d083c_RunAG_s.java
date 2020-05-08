 package tests;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.PriorityQueue;
 import java.util.Random;
 import java.util.Scanner;
 
 import geneticalgorithm.Chromosome;
 import geneticalgorithm.CompMaximize;
 import geneticalgorithm.CompMinimize;
 
 public class RunAG
 {
 
     /**
      * @param args
      */
     public static void main(String[] args)
     {
         double mutation = 0.05;
         double elitep = 0.10;
         double txcross = 0;
        int pop = 50;
        int cycles = 50;
         int i, k;
        int elite = (int) Math.floor(pop*(elitep));
         int op = 100;
         Chromosome o_pop[], n_pop[], tmp[];      
         Scanner sc = new Scanner(System.in);
         Comparator<Chromosome> comp = null;
 
         
         /* Seting up variables */
         mutation=-1; elitep=-1; txcross=-1; cycles=-1;
 
        while (txcross < 0 || txcross > 1)
         {
         	System.out.printf("\nEscolha a taxa de crossover, inteiro de 0 a 100 : ");
         	txcross = sc.nextInt()/100.0;
         }
 
        while (mutation < 0 || mutation > 1)
         {
         	System.out.printf("\nEscolha a taxa de mutacao, inteiro de 0 a 100 : ");
         	mutation = sc.nextInt()/100.0;
         }
 
        while (elitep < 0 || mutation > 1)
         {
         	System.out.printf("\nEscolha a taxa de elitismo, inteiro de 0 a 100 : ");
         	elitep = sc.nextInt()/100.0;
         }
 
        while (elitep < 0 || mutation > 1)
         {
        	System.out.printf("\nEscolha o tamanho da população, inteiro de 1 a %d :", Integer.MAX_VALUE);
         	pop = sc.nextInt();
         }
 
         System.out.print("\nEscolha o número de bits do cromossomo: ");
         Chromosome.setBITS(sc.nextInt());
 
         System.out.printf("0 - Sair\n");
         System.out.printf("1 - Maximizar funcao\n");
         System.out.printf("2 - Minimizar funcao\n");
         System.out.printf("> ");
         op = sc.nextInt();
 
         switch(op)
         {
         case 1:
             while (cycles < 1)
             {
                 comp = new CompMaximize();
                 System.out.printf("\nMaximizar funcao escolhido");
                 System.out.printf("\nQuantos ciclos rodar: ");
                 cycles = sc.nextInt();
             }
             break;
         case 2:
             while (cycles < 1)
             {
                 comp = new CompMinimize();
                 System.out.printf("\n\nMinimizar funcao escolhido");
                 System.out.printf("\nQuantos ciclos rodar: ");
                 cycles = sc.nextInt();
             }
             break;
         default:
         	while (op != 1 || op != 2)
         	{
 	            System.out.println("Opção " + op + "inválida, por favor escolha:");
 	            System.out.printf("0 - Sair\n");
 	            System.out.printf("1 - Maximizar funcao\n");
 	            System.out.printf("2 - Minimizar funcao\n");
 	            System.out.printf("> ");
 	            op = sc.nextInt();
         	}
             break;
         }
         /* End set up variables */
         
         /* Starting Algorithm */
         
         o_pop = new Chromosome[pop];
         n_pop = new Chromosome[pop];
         
         System.out.println("Function to maximize: f(x) = -(x-1)^2 +5");
         for(int x = -10; x < 21; x++)
             System.out.printf("f(%d) = %f\n", x, -1.0*Math.pow(x-1.0, 2.0)+5.0);
 
         System.out.printf("Initializing....\n");
         for(i = 0; i < pop; i++)
         {
             o_pop[i] = new Chromosome();
             System.out.printf("[%d] %s: %f\n", i, o_pop[i], o_pop[i].getValue());
         }
 
         for(i = 0; i < cycles; i++)
         {
             System.out.printf("==================Generation %d==================\n", i);
             /* Evaluate */
             for(k = 0; k < pop; k++)
                 o_pop[k].setRank(evaluate(o_pop[k]));
 
             /* Sort the vector using Rank*/
             Arrays.sort(o_pop, comp);
 
             /* Put the elite in the new population vector */
             System.out.println("População:");
             for(k = 0; k < elite; k++)
             {
                 n_pop[k] = o_pop[k];
                 System.out.printf("Elite[%d] = f(%f) = %f {%s}\n", k, o_pop[k].getValue(), o_pop[k].getRank(), o_pop[k]);
             }
 
             /* Print everyone */
             for(k = elite; k < pop; k++)
                 System.out.printf("      [%d] = f(%f) = %f {%s}\n", k, o_pop[k].getValue(), o_pop[k].getRank(), o_pop[k]);
 
             /* Do a contest */
             tmp = Chromosome.contest(o_pop, (pop-elite), 5, comp);
 
             /* Put the result of the contest in n_pop */
             for(k = 0; k < pop-elite; k++)
             {
                 //Apply crossover, or not
                 if(Math.random() <= txcross)
                     n_pop[k+elite] = tmp[k];
                 else
                     n_pop[k+elite] = o_pop[k+elite];
             }
 
             /* Mutate */
             for(k=elite; k < pop; k++)
                 n_pop[k].mutation(mutation);
 
             o_pop = n_pop;
             System.out.flush();
         }
 
         /* Evaluate the last population */
         for(k = 0; k < pop; k++)
             o_pop[k].setRank(evaluate(o_pop[k]));
 
         /* Sort the vector using Rank*/
         Arrays.sort(o_pop, comp);
 
         System.out.println("\n\nTerminado!\n");
         System.out.println("A última população é: ");
         /* Put the elite in the new population vector */
         for(k = 0; k < elite; k++)
             System.out.printf("Elite[%d] = f(%f) = %f {%s}\n", k, o_pop[k].getValue(), o_pop[k].getRank(), o_pop[k]);
 
         /* Print everyone */
         for(k = elite; k < pop; k++)
             System.out.printf("      [%d] = f(%f) = %f {%s}\n", k, o_pop[k].getValue(), o_pop[k].getRank(), o_pop[k]);
 
         System.out.println("\n\nA melhor solução é:\n");
         System.out.printf("f(%f) = %f\nChromossomo: %s\n", o_pop[0].getValue(), o_pop[0].getRank(), o_pop[0]);
     }
 
     public static double evaluate(Chromosome c)
     {
         double v, x = c.getValue();
 
         v = -1.0*Math.pow(x-1.0, 2.0)+5.0; //it's f(x) = -(x-1)^2 +5
 
         return v;
     }    
 }
