 import java.io.File;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 //PROCESSOR DATA
 class processor{
 	int no_of_proc;
 	int[][] comm_delay;
 	
 	processor(){
 		no_of_proc = 0;
		comm_delay = new int[5][5];
 	}
 }
 
 //TASK DATA
 class tasks{
 	int no_of_task;
 	int[][] comm_cost;
 	int[][] exe_time;
 	
 	tasks(){
 		no_of_task = 0;
		comm_cost = new int[20][20];
		exe_time = new int[20][5];
 	}
 }
 
 // TAKE INPUT THROUGH FILE
 class inputData {
	tasks inputTask;
	processor inputProc;
 	
	inputData()	{
		inputTask = new tasks();
		inputProc = new processor();
	}
 	
 	//TAKES INPUT FROM FILE INPUT.TXT
 	public void takeinput() 
 	{
 		try 
         	{
         	    	File file = new File("input.txt");
         	    	Scanner scanner = new Scanner(file);
         	    	
         	    	//INPUT NO. OF TASKS AND PROCESSORS
         	    	String line = scanner.nextLine();
         	    	StringTokenizer st=new StringTokenizer(line," ");
         	    	inputTask.no_of_task = Integer.parseInt(st.nextToken());
         	    	inputProc.no_of_proc = Integer.parseInt(st.nextToken());
        	    	line = scanner.nextLine(); //for \n
         	    	//System.out.println("No. of Task = "+ inputTask.no_of_task);
         	    	//System.out.println("No. of Processor = "+ inputProc.no_of_proc);
         	    	
         	    	//INPUT COMPUTATIONAL COST(EXECUTION TIME) OF TASK ON EACH PROCESSOR
         	    	int i=0,j=0;
         	    	//System.out.print("Tasks Execution time "+inputTask.no_of_task+"x"+inputProc.no_of_proc+" = ");
         	    	for(i=0;i<inputTask.no_of_task;i++)
         	    	{
         	    	   	line = scanner.nextLine();
 	        	    	st=new StringTokenizer(line," ");
 	        	    	//System.out.print("(");
 	        	    	for(j=0;j<inputProc.no_of_proc;j++)
 	        	    	{
 	        	    		inputTask.exe_time[i][j] = Integer.parseInt(st.nextToken());
 	        	    		//System.out.print(inputTask.exe_time[i][j]+",");
 	        	    	}
 	        	    	//System.out.print(")\t");
 	        	    	//System.out.println();
         	    	}
         	    	//System.out.println();
         	    	line = scanner.nextLine(); //for \n
         	    	
         	    	//INPUT COMMUNICATION DELAY BETWEEN PROCESSORS 
         	    	//System.out.print("Communication delay of Processors "+inputProc.no_of_proc+"x"+inputProc.no_of_proc+" = ");
         	    	for(i=0;i<inputProc.no_of_proc;i++)
         	    	{
         	    	   	line = scanner.nextLine();
 	        	    	st=new StringTokenizer(line," ");
 	        	    	//System.out.print("(");
 	        	    	for(j=0;j<inputProc.no_of_proc;j++)
 	        	    	{
 	        	    		inputProc.comm_delay[i][j] = Integer.parseInt(st.nextToken());
 	        	    		//System.out.print(inputProc.comm_delay[i][j]+",");
 	        	    	}
 	        	    	//System.out.print(")\t");
 	        	    	//System.out.println();
         	    	}
         	    	//System.out.println();
         	    	line = scanner.nextLine(); //for \n
         	    	
         	    	//INPUT DATA VOLUME WHICH IS TRANSMITTED BETWEEN TWO TASK
         	    	//System.out.println("Communication cost between tasks "+inputTask.no_of_task+"x"+inputTask.no_of_task+" = ");
         	    	for(i=0;i<inputTask.no_of_task;i++)
         	    	{
         	    	   	line = scanner.nextLine();
 	        	    	st=new StringTokenizer(line," ");
 	        	    	for(j=0;j<inputTask.no_of_task;j++)
 	        	    	{
 	        	    		inputTask.comm_cost[i][j] = Integer.parseInt(st.nextToken());
 	        	    		//System.out.print(inputTask.comm_cost[i][j]+" ");
 	        	    	}
 	        	    	//System.out.println();
         	    	}
         	    	//System.out.println(inputTask.no_of_task+"   "+inputProc.no_of_proc);
             
         	}
 		catch (Exception e) {
     	    e.printStackTrace();
     	}
         	  
 	}
 }
 
 //TASK STARTING TIME , FINISH TIME AND PROCESSOR AFTER SCHEDULE
 class scheduleTask {
 	int ST;
 	int FT;
 	int taskNo;
 	int proc;
 	scheduleTask()
 	{
 		
 	}
 }
 
 //Main class
 public class DAG {	
 	static int max,i,minScheduleID=0;
 	
 	//HEIGHT OF EACH NODE(FROM ROOT)
 	static int[] height;
 	//DECLARE AND DEFINE POP SIZE
 	static int pop_size = 100;
 	//Solution set (Population EQUAL TO POP_SIZE)
 	static int [][][] chromosomes; 		 
 	//Processor - Task list of Min Schedule
 	static int minScheduleList[][];			
 	//TASK PRECEDENCE INT THE ORDER OF THEIR LEVEL
 	static int[] levelOrder;						  
 	//CREATE OBJECT OF INPUT
 	static inputData input1 = new inputData();					  
 	//PROCESSOR AVAILABLE TIME
 	static int procAvT[];							
 	//STORE SCHEDULE TASK DATA
 	static scheduleTask taskData[];		 
 	//STORE WHICH TASK SCHEDULE AT WHICH PROCESSOR
 	static int taskAtProc[][];
 	//INITIALIZE THE MAKESPAN WITH A HIGHER VALUE
 	static float minScheduleTime=10000;
 	//STORES FINAL PROCESSOR AVAILABLE TIME OF EACH PROCESSORS
 	static int finalprocAvT[];
 	
 	//Calculate Height of each node
 	static int heightOfNode(int node)
 	{
 		if(node==0)
 		{
 			return 0;
 		}
 		else
 		{
 			max=0;
 			for(i=input1.inputTask.no_of_task;i>=1;i--)
 			{
 				if(input1.inputTask.comm_cost[i-1][node]>0 && max < height[i-1])
 					max =  height[i-1];
 			}	
 			return (1+max);
 		}
 		
 	}
 	
 	//Calculate level order of each node and store it in height[]
 	static void calcLevelOrder()
 	{
 		int i=0,j=0,tmp;
 		int[] tmpHeight =new int[input1.inputTask.no_of_task];
 		for(i=0;i<input1.inputTask.no_of_task;i++)
 		{
 			levelOrder[i]=i;
 			tmpHeight[i]=height[i];
 		}
 		for(i=0;i<input1.inputTask.no_of_task;i++)
 		{
 			for(j=i+1;j<input1.inputTask.no_of_task;j++)
 			{
 				if(tmpHeight[i]>tmpHeight[j])
 				{
 					tmp=tmpHeight[i];
 					tmpHeight[i]=tmpHeight[j];
 					tmpHeight[j]=tmp;
 					
 					tmp=levelOrder[i];
 					levelOrder[i]=levelOrder[j];
 					levelOrder[j]=tmp;
 					//System.out.print("roy");
 				}
 				
 			}
 		}
 		/*	
 	 	System.out.print("Level Order : ");
 		for(i=0;i<input1.inputTask.no_of_task;i++)
 		{
 			System.out.print("_"+levelOrder[i]);
 		}*/
 	}
 	
 	//CREATE POPULATION OF 1000 CHROMOSOMES AND STORE IT IN CHROMOSOMES[][][]
 	static void createPopulation()
 	{	
 		int k,tmp,l,z;
 		Random rand = new Random();
 		int counter[] = new int[input1.inputProc.no_of_proc];
 		calcLevelOrder();
 		for(l=0;l<pop_size;l++) // Populatio 1000
 		{
 			for(z=0;z<input1.inputProc.no_of_proc;z++)
 			{
 				counter[z]=0;
 			}
 			for(k=0;k<input1.inputTask.no_of_task;k++)
 			{
 					tmp = rand.nextInt(input1.inputProc.no_of_proc);	
 					chromosomes[l][tmp][counter[tmp]] = levelOrder[k];	
 					taskAtProc[l][levelOrder[k]]=tmp;
 					
 					counter[tmp]++;
 			}
 			//System.out.println();
 			/*
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 					System.out.print(chromosomes[l][i][j]);
 				System.out.print(" ");
 			}
 			*/
 			//for(k=0;k<input1.inputTask.no_of_task;k++)
 			//{
 				//System.out.print(taskAtProc[k]+" ");
 			//}
 			//System.out.println();
 		}
 	}
 
 	//Calculate max of two numbers
 	static int maX(int x,int y)
 		{
 			return x>y?x:y;
 		}
 	
 	//CALCULATE FITNESS OF A GIVEN CHROMOSOME 
 	static float fitNess(int id)
 	{
 		int i=0,j,z,maxF;
 		float fitness=0;
 		float sum=0;
 		for(i=0;i<input1.inputTask.no_of_task;i++)
 			taskData[i]=new scheduleTask();
 			
 		for(z=0;z<input1.inputProc.no_of_proc;z++)
 		{
 			procAvT[z]=0;
 		}
 		
 		for(i=0;i<input1.inputTask.no_of_task;i++)
 		{
 			int maxDAT=0;
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{				
 				if(input1.inputTask.comm_cost[j][levelOrder[i]]>0 && maxDAT < (taskData[levelOrder[j]].FT+(input1.inputProc.comm_delay[taskAtProc[id][levelOrder[j]]][taskAtProc[id][levelOrder[i]]]*input1.inputTask.comm_cost[j][levelOrder[i]])))
 					maxDAT =  (taskData[levelOrder[j]].FT+(input1.inputProc.comm_delay[taskAtProc[id][levelOrder[j]]][taskAtProc[id][levelOrder[i]]]*input1.inputTask.comm_cost[j][levelOrder[i]]));
 			}	
 			taskData[levelOrder[i]].taskNo = levelOrder[i];
 			taskData[levelOrder[i]].proc = taskAtProc[id][levelOrder[i]];
 			taskData[levelOrder[i]].ST =maX( procAvT[taskAtProc[id][levelOrder[i]]] , maxDAT);
 			//System.out.print(taskData[levelOrder[i]].ST+" ");
 			taskData[levelOrder[i]].FT = taskData[levelOrder[i]].ST+input1.inputTask.exe_time[levelOrder[i]][taskAtProc[id][levelOrder[i]]];
 			//System.out.print(taskData[levelOrder[i]].FT+" ");
 			procAvT[taskAtProc[id][levelOrder[i]]]=taskData[levelOrder[i]].FT;
 		}
 		//System.out.print(":");
 		maxF = 0;
 		sum =0;
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			sum+=procAvT[i];
 			if(maxF<procAvT[i])
 				maxF=procAvT[i];
 		}
 		//for(i=0;i<5;i++)
 		//{
 		//	System.out.print(procAvT[i]+" ");
 		//}
 		
 		fitness = maxF/(sum/(input1.inputProc.no_of_proc*maxF));				
 		return fitness;
 	}
 	
 	
 	//SINGLE POINT CROSSOVER
 	static void crossOver(int id)
 	{
 		int proc1,proc2,length1=0,length2=0,i=0,j=0,divide1=0,divide2=0,tmp1,tmp2;		
 		Random rand = new Random();
 		proc1 = rand.nextInt(input1.inputProc.no_of_proc);
 		
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			//System.out.print(i+1+"th Processor :");
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{				
 				chromosomes[pop_size][i][j]=0;
 			}
 		}
 		for(j=0;j<input1.inputTask.no_of_task;j++)
 		{
 			taskAtProc[pop_size][j] = 0;
 		}
 		while(true)
 		{
 			proc2 = rand.nextInt(input1.inputProc.no_of_proc);
 			if(proc2!=proc1)
 				break;
 		}
 		while(i<input1.inputTask.no_of_task)
 		{
 			if(length1==0 && chromosomes[id][proc1][i]==0)
 				length1++;
 			else if(chromosomes[id][proc1][i]!=0)
 				length1++;
 			else
 				break;
 			i++;
 		}
 		i=0;
 		while(i<input1.inputTask.no_of_task)
 		{
 			if(length2==0 && chromosomes[id][proc2][i]==0)
 				length2++;
 			else if(chromosomes[id][proc2][i]!=0)
 				length2++;
 			else
 				break;
 			i++;
 		}
 		divide1 = rand.nextInt(length1);
 		while(levelOrder[divide1] >= chromosomes[id][proc2][divide2])
 		{			
 				divide2++;
 				if(divide2==input1.inputTask.no_of_task)
 					break;
 		}
 		//System.out.println(divide1+" "+divide2);
 		if(divide2!=input1.inputTask.no_of_task)
 		{
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					if(i!=proc1 && i!=proc2)
 						chromosomes[pop_size][i][j] = chromosomes[id][i][j];
 				}
 			}
 			tmp1=divide1;
 			tmp2=divide2;
 			//System.out.print(divide1+" "+divide2+"\t");
 			for(j=0;j<divide1;j++)
 			{
 						chromosomes[pop_size][proc1][j] = chromosomes[id][proc1][j];
 			}
 			for(j=0;j<divide2;j++)
 			{
 				chromosomes[pop_size][proc2][j] = chromosomes[id][proc2][j];
 			}
 			for(j=divide1;j<input1.inputTask.no_of_task && tmp2<input1.inputTask.no_of_task;j++)
 			{
 				//System.out.print("t2 "+tmp2);
 				chromosomes[pop_size][proc1][j] = chromosomes[id][proc2][tmp2];
 				tmp2++;
 			}
 			for(j=divide2;j<input1.inputTask.no_of_task && tmp1<input1.inputTask.no_of_task;j++)
 			{
 				//System.out.print("t1 "+tmp1);		
 				chromosomes[pop_size][proc2][j] = chromosomes[id][proc1][tmp1];
 				tmp1++;
 			}
 			/*
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					System.out.print(chromosomes[id][i][j]+" ");
 				}
 				System.out.println();
 			}
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					System.out.print(chromosomes[1000][i][j]+" ");
 				}
 				System.out.println();
 			}
 			*/
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					if(chromosomes[pop_size][i][j]==0 && j!=0)
 						break;
 						taskAtProc[pop_size][chromosomes[pop_size][i][j]] = i;
 				}
 			}
 			if(minScheduleTime > fitNess(pop_size))
 			{
 				minScheduleTime = fitNess(pop_size);
 				
 				for(i=0;i<input1.inputProc.no_of_proc;i++)
 				{
 					finalprocAvT[i]=procAvT[i];
 					//System.out.print(i+1+"th Processor :");
 					for(j=0;j<input1.inputTask.no_of_task;j++)
 					{				
 						minScheduleList[i][j]=0;
 					}
 				}
 				for(i=0;i<input1.inputProc.no_of_proc;i++)
 				{
 					//System.out.print(i+1+"th Processor :");
 					for(j=0;j<input1.inputTask.no_of_task;j++)
 					{				
 						minScheduleList[i][j]=chromosomes[pop_size][i][j];
 						chromosomes[id][i][j]=chromosomes[pop_size][i][j];
 					}
 				}	
 			}
 		}
 		//System.out.println(length1+" "+length2);
 	}
 
 	//Mutation of a given chromosome and calculate its Fitness
 	static void mutaTion(int id)
 	{
 		int i=0,j,z,scheduleTime=0,procNo=0,diff=0,maxDiff=0,nxtTask=0,tmp=0,parent=0;
 			
 		for(z=0;z<input1.inputProc.no_of_proc;z++)
 		{
 			procAvT[z]=0;
 		}
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			//System.out.print(i+1+"th Processor :");
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{				
 				chromosomes[pop_size][i][j]=0;
 			}
 		}
 		for(j=0;j<input1.inputTask.no_of_task;j++)
 		{
 			taskAtProc[pop_size][j] = 0;
 		}
 		for(i=0;i<input1.inputTask.no_of_task;i++)
 		{
 			int maxDAT=0;
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{				
 				if(input1.inputTask.comm_cost[j][levelOrder[i]]>0 && maxDAT < (taskData[levelOrder[j]].FT+(input1.inputProc.comm_delay[taskAtProc[id][levelOrder[j]]][taskAtProc[id][levelOrder[i]]]*input1.inputTask.comm_cost[j][levelOrder[i]])))
 					maxDAT =  (taskData[levelOrder[j]].FT+(input1.inputProc.comm_delay[taskAtProc[id][levelOrder[j]]][taskAtProc[id][levelOrder[i]]]*input1.inputTask.comm_cost[j][levelOrder[i]]));
 			}	
 			taskData[levelOrder[i]].taskNo = levelOrder[i];
 			taskData[levelOrder[i]].proc = taskAtProc[id][levelOrder[i]];
 			taskData[levelOrder[i]].ST =maX( procAvT[taskAtProc[id][levelOrder[i]]] , maxDAT);
 			//System.out.print(taskData[levelOrder[i]].ST+" ");
 			taskData[levelOrder[i]].FT = taskData[levelOrder[i]].ST+input1.inputTask.exe_time[levelOrder[i]][taskAtProc[id][levelOrder[i]]];
 			//System.out.print(taskData[levelOrder[i]].FT+" ");
 			procAvT[taskAtProc[id][levelOrder[i]]]=taskData[levelOrder[i]].FT;
 		}
 		//System.out.print(":");
 		scheduleTime=0;
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			if(scheduleTime<procAvT[i])
 			{
 				scheduleTime=procAvT[i];
 				procNo = i;
 			}
 			
 		}
 		//System.out.print(procNo+" ");
 		i=0;
 		maxDiff = taskData[chromosomes[id][procNo][i]].ST;
 		//System.out.print(maxDiff+" ");
 		while(i<input1.inputTask.no_of_task && chromosomes[id][procNo][i+1]!=0)
 		{
 			diff = taskData[chromosomes[id][procNo][i+1]].ST -taskData[chromosomes[id][procNo][i]].FT;
 			if(maxDiff < diff )
 			{
 				maxDiff = diff;
 				nxtTask = chromosomes[id][procNo][i+1];
 			}
 			i++;
 			//System.out.print(maxDiff);
 		}
 		//System.out.print(" "+nxtTask+" ");
 		
 		tmp =0 ;
 		for(j=0;j<input1.inputTask.no_of_task;j++)
 		{				
 			if(tmp < input1.inputTask.comm_cost[j][nxtTask])
 			{	
 				tmp = input1.inputTask.comm_cost[j][nxtTask];
 				parent = j;
 			}
 		}
 		//System.out.print(parent);
 		if(nxtTask==0 || procNo ==taskAtProc[id][parent] )
 		{
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					chromosomes[pop_size][i][j] = chromosomes[id][i][j];
 				}
 			}
 		}
 		
 		else{	
 	
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					if(i!=taskAtProc[id][parent] && i!=procNo)
 						chromosomes[pop_size][i][j] = chromosomes[id][i][j];
 				}
 			}
 			tmp = 0;
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{
 				if(chromosomes[id][procNo][j]==nxtTask)
 				{
 					j++;
 				}
 					chromosomes[pop_size][procNo][tmp] = chromosomes[id][procNo][j];
 					tmp++;
 			}
 			chromosomes[pop_size][procNo][tmp]=0;
 			tmp=0;
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{		
 				chromosomes[pop_size][taskAtProc[id][parent]][j] = chromosomes[id][taskAtProc[id][parent]][tmp];
 				//System.out.println(" "+chromosomes[1000][taskAtProc[id][parent]][j]);
 				tmp++;
 				if(tmp<input1.inputTask.no_of_task  && chromosomes[id][taskAtProc[id][parent]][tmp]==parent)
 				{
 					j++;
 					while(height[chromosomes[id][taskAtProc[id][parent]][tmp]] < height[nxtTask] && chromosomes[id][taskAtProc[id][parent]][tmp]!=0 && j<input1.inputTask.no_of_task && tmp< input1.inputTask.no_of_task  )
 					{				
 						chromosomes[pop_size][taskAtProc[id][parent]][j] = chromosomes[id][taskAtProc[id][parent]][tmp];
 						//System.out.println(" "+chromosomes[1000][taskAtProc[id][parent]][j]);
 						tmp++;
 						j++;
 					}
 					chromosomes[pop_size][taskAtProc[id][parent]][j] = nxtTask;
					//System.out.println(" "+chromosomes[1000][taskAtProc[id][parent]][j]);
 				}		
 			}
 		}
 		/*System.out.println();
 		
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{
 				System.out.print(chromosomes[id][i][j]+" ");
 			}
 			System.out.println();
 		}
 		System.out.println();
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{
 				System.out.print(chromosomes[1000][i][j]+" ");
 			}
 			System.out.println();
 		}
 		*/
 		//System.out.println("\n"+fitNess(id)+" "+fitNess(1000));
 		for(i=0;i<input1.inputProc.no_of_proc;i++)
 		{
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{
 				if(chromosomes[pop_size][i][j]==0 && j!=0)
 					break;
 					taskAtProc[pop_size][chromosomes[pop_size][i][j]] = i;
 			}
 		}
 		if(minScheduleTime > fitNess(pop_size))
 		{
 			minScheduleTime = fitNess(pop_size);
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				finalprocAvT[i]=procAvT[i];
 				//System.out.print(i+1+"th Processor :");
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{				
 					minScheduleList[i][j]=0;
 				}
 			}
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				//System.out.print(i+1+"th Processor :");
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{				
 					minScheduleList[i][j]=chromosomes[pop_size][i][j];
 					chromosomes[id][i][j]=chromosomes[pop_size][i][j];
 				}
 			}
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{
 					taskAtProc[id][j] = taskAtProc[pop_size][j];
 				}
 			}
 			
 		}
 		//System.out.println("\n"+fitNess(id)+" "+fitNess(1000));
 	}
 	
 	//MAIN METHOD
 	public static void main(String args[])
 		{
 			int i,j=0,k;
 			
 			float fitnessValue;
 			//NO OF GENERATIONS
 			int gen=5;
 			Random rand = new Random();
 			input1.takeinput();
 			
 			height= new int[input1.inputTask.no_of_task];
 			chromosomes = new int[pop_size+1][input1.inputProc.no_of_proc][input1.inputTask.no_of_task];
 			minScheduleList = new int[input1.inputProc.no_of_proc][input1.inputTask.no_of_task];
 			levelOrder =new int[input1.inputTask.no_of_task];
 			procAvT = new int[input1.inputProc.no_of_proc];
 			taskData = new scheduleTask[input1.inputTask.no_of_task];
 			taskAtProc = new int[pop_size+1][input1.inputTask.no_of_task];
 			finalprocAvT = new int[input1.inputProc.no_of_proc];
 			
 			//System.out.print("Height of each node : ");
 			for(j=0;j<input1.inputTask.no_of_task;j++)
 			{	
 				height[j] = heightOfNode(j);
 				//System.out.print(" "+height[j]);
 			}
 			//System.out.println();
 			
 			//POPULATION CREATION
 			createPopulation(); 
 			//System.out.println();
 			//System.out.println("Schedule Time of each chromosome : ");
 			//CALCULATION FITNESS OF CHROMOSOMES
 			for(int l = 0;l<pop_size;l++)
 			{
 				fitnessValue = fitNess(l);
 				//System.out.print(fitnessValue+" ");			
 				
 				if(minScheduleTime>fitnessValue)
 				{
 					minScheduleTime = fitnessValue;		
 					for(i=0;i<input1.inputProc.no_of_proc;i++)
 					{
 						finalprocAvT[i]=procAvT[i];
 						for(j=0;j<input1.inputTask.no_of_task;j++)
 						{
 							minScheduleList[i][j]=chromosomes[l][i][j];
 							//System.out.print(minScheduleList[i][j]);
 						}
 					}
 				}
 			}
 			//System.out.println();
 			//BEFORE CROSSOVER MIN SCHEDULE
 			System.out.println("Before Crossover :");
 			
 			//System.out.println("Min Schedule Time = " + minScheduleTime);
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				System.out.print(i+1+"th Processor :");
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{				
 					if(minScheduleList[i][j]==0 && j!=0)
 					{
 						break;					
 					}					
 					System.out.print(" "+minScheduleList[i][j]);
 					
 				}System.out.print(" = "+finalprocAvT[i]+"\n");
 			}
 
 			float maxZ=0,sum=0;
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				sum+=finalprocAvT[i];
 				if(maxZ<finalprocAvT[i])
 					maxZ=finalprocAvT[i];
 			}
 			System.out.println("Schedule Time = "+maxZ);
 			System.out.println("Utilization = "+sum/(input1.inputProc.no_of_proc*maxZ));
 			
 			// CROSSOVER
 			for(k=0;k<gen;k++)
 			{
 				for(i=0;i<pop_size;i++)
 				{			
 					crossOver(rand.nextInt(pop_size));	
 				}
 				/*After crossover Min Schedule
 				System.out.println("\nAfter Crossover :");
 				System.out.println("Min Schedule Time " + minScheduleTime);
 			
 				for(i=0;i<input1.inputProc.no_of_proc;i++)
 				{
 					System.out.print(i+1+"th Processor :");
 					for(j=0;j<input1.inputTask.no_of_task;j++)
 					{				
 						if(minScheduleList[i][j]==0 && j!=0)
 						{
 							break;					
 						}					
 						System.out.print(" "+minScheduleList[i][j]);
 					}System.out.print(" = "+finalprocAvT[i]+"\n");
 				}
 				maxZ=0;sum=0;
 				for(i=0;i<input1.inputProc.no_of_proc;i++)
 				{
 					sum+=finalprocAvT[i];
 					if(maxZ<finalprocAvT[i])
 						maxZ=finalprocAvT[i];
 				}
 				System.out.println("Schedule Time = "+maxZ);
 				System.out.println("Utilization = "+sum/(input1.inputProc.no_of_proc*maxZ));*/
 	
 				//MUTATION
 				for(i=0;i<pop_size;i++)
 				{			
 					mutaTion(rand.nextInt(pop_size));	
 				}
 				/*After Mutation Min Schedule
 				System.out.println("\nAfter Mutation :");
 				System.out.println("Min Schedule Time " + minScheduleTime);*/
 			
 				/*for(i=0;i<input1.inputProc.no_of_proc;i++)
 				{
 					System.out.print(i+1+"th Processor :");
 					for(j=0;j<input1.inputTask.no_of_task;j++)
 					{				
 						if(minScheduleList[i][j]==0 && j!=0)
 						{
 							break;					
 						}					
 						System.out.print(" "+minScheduleList[i][j]);
 					}System.out.print(" = "+finalprocAvT[i]+"\n");
 				}
 				maxZ=0;sum=0;
 				for(i=0;i<input1.inputProc.no_of_proc;i++)
 				{
 					sum+=finalprocAvT[i];
 					if(maxZ<finalprocAvT[i])
 						maxZ=finalprocAvT[i];
 				}*/
 				//System.out.println("Schedule Time = "+maxZ);
 				//System.out.println("Utilization = "+sum/(input1.inputProc.no_of_proc*maxZ));
 				
 				//System.out.print(fitNess(0)+" "+fitNess(pop_size));
 			}
 			System.out.println("\nAfter Mutation :");
 			//System.out.println("Min Schedule Time " + minScheduleTime);
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				System.out.print(i+1+"th Processor :");
 				for(j=0;j<input1.inputTask.no_of_task;j++)
 				{				
 					if(minScheduleList[i][j]==0 && j!=0)
 					{
 						break;					
 					}					
 					System.out.print(" "+minScheduleList[i][j]);
 				}System.out.print(" = "+finalprocAvT[i]+"\n");
 			}
 			maxZ=0;sum=0;
 			for(i=0;i<input1.inputProc.no_of_proc;i++)
 			{
 				sum+=finalprocAvT[i];
 				if(maxZ<finalprocAvT[i])
 					maxZ=finalprocAvT[i];
 			}
 			System.out.println("Schedule Time = "+maxZ);
 			System.out.println("Utilization = "+sum/(input1.inputProc.no_of_proc*maxZ));
 		}
 }	
