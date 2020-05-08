 public class Main{
 	public static void main(String args[]){
 		Programa[] programas = new Programa[4];
		Mapa mapa = new Mapa("/home/ruan/Documentos/labprog2/ep2-labprog2/bin/../data/map/mapa.txt");
 	    Arena arena = new Arena(mapa);
 	    Programa programa;
 
 
 programas = new Programa[1];
 programa = new Programa();
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("PUSH", new Numero(2) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("PUSH", new Numero(3) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("PUSH", new Numero(4) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("PUSH", new Numero(5) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("PUSH", new Numero(6) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("JMP", new Numero( 0 )));
 programas[0] = programa;
 arena.insereExercito(programas, 1);
 programas = new Programa[1];
 programa = new Programa();
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("STO", new Numero(1) ));
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("STO", new Numero(0) ));
 programa.add( new Comando("RCL", new Numero(0) ));
 programa.add( new Comando("LOOK", null));
 programa.add( new Comando("PUSH", new Numero(4) ));
 programa.add( new Comando("ISA", null));
 programa.add( new Comando("JIT", new Numero( 32 )));
 programa.add( new Comando("RCL", new Numero(0) ));
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("ADD", null));
 programa.add( new Comando("STO", new Numero(0) ));
 programa.add( new Comando("RCL", new Numero(0) ));
 programa.add( new Comando("PUSH", new Numero(7) ));
 programa.add( new Comando("EQ", null));
 programa.add( new Comando("JIT", new Numero( 18 )));
 programa.add( new Comando("JMP", new Numero( 4 )));
 programa.add( new Comando("RCL", new Numero(1) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("JIT", new Numero( 2 )));
 programa.add( new Comando("RCL", new Numero(1) ));
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("ADD", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("STO", new Numero(1) ));
 programa.add( new Comando("PUSH", new Numero(7) ));
 programa.add( new Comando("EQ", null));
 programa.add( new Comando("JIF", new Numero( 18 )));
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("STO", new Numero(1) ));
 programa.add( new Comando("JMP", new Numero( 18 )));
 programa.add( new Comando("RCL", new Numero(0) ));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("COLLECT", null));
 programa.add( new Comando("JIF", new Numero( 2 )));
 programa.add( new Comando("PUSH", new Frase("GG") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("JMP", new Numero( 41 )));
 programa.add( new Comando("END", null));
 programa.add( new Comando("PUSH", new Numero(2) ));
 programa.add( new Comando("GETTIME", null));
 programa.add( new Comando("GETBASE", null));
 programa.add( new Comando("GETPOS", null));
 programa.add( new Comando("STO", new Numero(3) ));
 programa.add( new Comando("RCL", new Numero(3) ));
 programa.add( new Comando("GETX", null));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LOOK", null));
 programa.add( new Comando("GETPOS", null));
 programa.add( new Comando("GETX", null));
 programa.add( new Comando("SUB", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LT", null));
 programa.add( new Comando("JIT", new Numero( 63 )));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("GT", null));
 programa.add( new Comando("JIT", new Numero( 68 )));
 programa.add( new Comando("PUSH", new Frase("WATAFUQ") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("JMP", new Numero( 82 )));
 programa.add( new Comando("PUSH", new Frase("CIMA") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("STO", new Numero(4) ));
 programa.add( new Comando("JMP", new Numero( 72 )));
 programa.add( new Comando("PUSH", new Frase("BAIXO") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(4) ));
 programa.add( new Comando("STO", new Numero(4) ));
 programa.add( new Comando("RCL", new Numero(4) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LOOK", null));
 programa.add( new Comando("GETPOS", null));
 programa.add( new Comando("GETX", null));
 programa.add( new Comando("RCL", new Numero(3) ));
 programa.add( new Comando("GETX", null));
 programa.add( new Comando("EQ", null));
 programa.add( new Comando("JIF", new Numero( 72 )));
 programa.add( new Comando("PUSH", new Frase("QUASELA") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("RCL", new Numero(3) ));
 programa.add( new Comando("GETY", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LOOK", null));
 programa.add( new Comando("GETPOS", null));
 programa.add( new Comando("GETY", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("STO", new Numero(5) ));
 programa.add( new Comando("SUB", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("GT", null));
 programa.add( new Comando("JIT", new Numero( 107 )));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LT", null));
 programa.add( new Comando("JIT", new Numero( 114 )));
 programa.add( new Comando("JMP", new Numero( 145 )));
 programa.add( new Comando("PUSH", new Frase("DIREITA") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(2) ));
 programa.add( new Comando("STO", new Numero(6) ));
 programa.add( new Comando("PUSH", new Numero(3) ));
 programa.add( new Comando("STO", new Numero(7) ));
 programa.add( new Comando("JMP", new Numero( 120 )));
 programa.add( new Comando("PUSH", new Frase("ESQUERDA") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(6) ));
 programa.add( new Comando("STO", new Numero(6) ));
 programa.add( new Comando("PUSH", new Numero(5) ));
 programa.add( new Comando("STO", new Numero(7) ));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LOOK", null));
 programa.add( new Comando("GETPOS", null));
 programa.add( new Comando("GETY", null));
 programa.add( new Comando("PUSH", new Numero(2) ));
 programa.add( new Comando("MOD", null));
 programa.add( new Comando("DUP", null));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Numero(1) ));
 programa.add( new Comando("EQ", null));
 programa.add( new Comando("JIT", new Numero( 134 )));
 programa.add( new Comando("RCL", new Numero(7) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("JMP", new Numero( 136 )));
 programa.add( new Comando("RCL", new Numero(6) ));
 programa.add( new Comando("WALK", null));
 programa.add( new Comando("RCL", new Numero(3) ));
 programa.add( new Comando("GETY", null));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("LOOK", null));
 programa.add( new Comando("GETPOS", null));
 programa.add( new Comando("GETY", null));
 programa.add( new Comando("EQ", null));
 programa.add( new Comando("JIT", new Numero( 145 )));
 programa.add( new Comando("JMP", new Numero( 120 )));
 programa.add( new Comando("PUSH", new Numero(0) ));
 programa.add( new Comando("DROP", null));
 programa.add( new Comando("PUSH", new Frase("GG2") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Frase("TENHO") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Frase("QUE") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Frase("PEGAR") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("PUSH", new Frase("MAIS") ));
 programa.add( new Comando("PRN", null));
 programa.add( new Comando("JMP", new Numero( 0 )));
 programa.add( new Comando("MYTIME", null));
 programa.add( new Comando("STO", new Numero(3) ));
 programas[0] = programa;
 arena.insereExercito(programas, 2);
 
 		int larguraJanela, alturaJanela;
 		if(mapa.largura()%2 == 1) larguraJanela = ((mapa.largura()-1)/2)*90 + 60;
 		else larguraJanela = mapa.largura()*90/2 + 15;
 		alturaJanela = (int)((mapa.altura()+1)*Math.sqrt(3)*30);
 
 		MapaVisual mv = new MapaVisual(mapa, alturaJanela, larguraJanela, 30);
 
 		mv.abreJanela();
 
 
 		while(arena.atualiza())
 		{
 			mv.atualiza();
 
 			try 
 			{
    				 Thread.sleep(100);
 			} 
 			catch(InterruptedException ex) {Thread.currentThread().interrupt();}
 		}
 
 		mv.gameOver();
 		
 	}
 }
 
