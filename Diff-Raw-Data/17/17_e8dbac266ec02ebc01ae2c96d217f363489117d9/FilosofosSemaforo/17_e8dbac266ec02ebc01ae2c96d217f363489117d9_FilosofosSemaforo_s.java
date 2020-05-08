 package pckThread;
 
 import java.util.Random;
 import java.util.concurrent.Semaphore;
 
 import main.Global;
 
 
 public class FilosofosSemaforo implements Runnable 
 {
 	private int nFilosofos = Global.nThreads;
 	Bakery semaforo = new Bakery(Global.nThreads);
 	private int id;
 	Semaphore sem = new Semaphore(1);
 
 	public FilosofosSemaforo(int id) 
 	{
 		this.id = id;
 	}
 	public int getID() { return this.id; }
 	public void run() 
 	{
 		Global.criarFilosofos();
 		Random randint = new Random(42);
 		int count = 0;
 		int posGarfo = this.getID();
 		this.info("iniciado");
 		try 
 		{
 			while(count <= Global.iteracoes)
 			{
 				//Todos comecam meditando
 				//this.info("Meditando");
 				Thread.sleep(Math.abs(randint.nextInt()%5000)*Global.aceleradorInvertido);
 				
 				//Regiao Critica para pegar os garfos
 				this.sem.acquire();
 				if (Global.getGarfo(posGarfo) == -1 && Global.getGarfo((posGarfo+1)%nFilosofos) == -1)				
 				{
 					//Pega um garfo para voce
 					Global.setGarfo(posGarfo, this.getID()); 
 					Global.setGarfo((posGarfo+1)%nFilosofos, this.getID());
 
 					//Se voce pegou um garfo, sai da regi‹o cr’tica
 					this.sem.release();
 
 					//Come
 					this.info("Comendo");
 					Thread.sleep(Math.abs(randint.nextInt()%500)*Global.aceleradorInvertido);
 					Global.fazRefeicao(this.getID());
 
 					//Regi‹o Cr’tica para soltar os garfos
 					this.sem.acquire();
 					//this.confereGarfos();
 					Global.setGarfo(posGarfo, -1); 
 					Global.setGarfo((posGarfo+1)%nFilosofos, -1);
 					this.info("Devolvi meus garfos");
 					this.sem.release();
 					//Saida da regi‹o cr’tica para soltar os garfos.
 				}
 				count++;
 			}
 			Global.mostraRefeicoes();
 		} catch (InterruptedException exc) 
 		{
 			this.info("Interrompido");
 		}
 		this.info("FIM");
 	}
 	public void info (String s)
 	{
 		System.out.println("Filosofo " + this.getID() + ": " + s);
 	}
 	public void confereGarfos ()
 	{
 		int i = 0;
 		int contador = 0 ;
 		for (i = 0; i< Global.length() ; i++)
 		{
 			if (Global.getGarfo(i) != -1) //Quantos garfos estao ocupados?
 			{
 				contador++;
 			}
 		}
 		if (contador <= 4)
 			this.info("Antes de soltar meus garfos, estava OK");
 		else
 		{
 			this.info("Opa, tem um erro aqui");
 			this.imprimeGarfos();
 		}
 	}
 	public void imprimeGarfos ()
 	{
 		int i = 0;
 		for (i = 0; i< Global.length() ; i++)
 		{
 			this.info(Global.getGarfo(i) + " ");
 		}
 	}
 }
