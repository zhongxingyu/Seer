 import java.util.*;
 import java.lang.Enum;
 
 public class Arena
 {
   private Mapa mapa;
   private List<Robo> robos;
   private int tempo;
   private int velocidade = 5;
   /****** Construtor ******/
 
   Arena(Mapa mapa)
   {
     this.mapa = mapa;
     tempo = 0;
     this.robos = new ArrayList<Robo>();//cria lista de robos vazia a principio
   }
 
   /****** Funções ******/
 
   /* Função que percorrerá a lista de robôs presentes na arena (em ordem aleatória). 
      Para cada robô encontrado, chamará uma função que executará o próximo comando   
      presente no programa do robô, dado o devido respeito às condições de atraso.    */
      public boolean atualiza()
      {
       ArrayList<Robo> robosMortos = new ArrayList<Robo>();
       Robo roboTemp;
       RndIterator<Robo> it;
       boolean alguemTemAcao = false;
       
       /* Percorre a lista de robôs, executando os comandos de cada um (de acordo com o estado de seu atraso). */
       for(it = new RndIterator<Robo>(robos); it.hasNext();)
       {
         roboTemp = it.next();
         if(!roboTemp.temVida())
           {
             robosMortos.add(roboTemp);
             continue;
           }
         if(!roboTemp.temAtraso())
         {
           for(int i = 0; i<velocidade && !roboTemp.temAtraso();i++)
             if(roboTemp.executaAcao())
             {
               alguemTemAcao = true;
             }
         }
         else
         {
           Atraso at = roboTemp.getAtraso();
           if(at.getTempo() <= 0)
           {
            this.sistema( at.getOperacao());
            roboTemp.tiraAtraso();
            for(int i = 0; i<velocidade && !roboTemp.temAtraso();i++) roboTemp.executaAcao();
          }
          else at.somaTempo(-1);
          alguemTemAcao = true;
        }
      }
    /* Incrementa a unidade que trata do tempo transcorrido desde o início do programa.                
    Ou seja, cada ciclo da função Atualiza representa uma unidade de tempo transcorrida no programa.*/
    this.tempo++;
 
 
 
    /*Remove da lista principal os robos mortos */
    //for(ListIterator<Robo> it2 = robosMortos.listIterator(); it2.hasNext(); robos.remove(it2.next()));
    robos.removeAll(robosMortos);
 
     ArrayList<Time> timesMortos = new ArrayList<Time>();
    /* Verifica se tem algum time que tenha já 3 cristais na base, e portanto tenha perdido */
    ArrayList<Time> times = mapa.getTimes();
    for(ListIterator<Time> it3 = times.listIterator();it3.hasNext();)
    {
       Time timeTemp = it3.next();
       if(timeTemp.getBase().numCristais() >= 1)
       {
         removeExercito(timeTemp.getId());
         System.out.println("Time "+timeTemp.getId()+" perdeu! Seu robôs irão para o inferno!");
         timesMortos.add(timeTemp);
       }
    }
 
    /*Remove os times perdedores*/
    times.removeAll(timesMortos);
 
    if(times.size() == 1)
    {
       String timeVencedor = null;
       switch(times.get(0).getId())
       {
         case 1: timeVencedor = "Vermelho";break;
         case 2: timeVencedor = "Azul";break;
         default:System.out.println("Não suportamos mais de 2 times");
       }
       System.out.println("O time "+timeVencedor+" ganhou!!");
       return false;
    }
 
    return alguemTemAcao;
  }
 
  /******
   Essa função recebe um objeto do tipo Operacao representando
   uma chamada ao sistema feita por algum robo.
   A função executa a ação necessária para executar a operação passada
   ***** */
   public void sistema(Operacao op)
   {
     Comando cmd = op.getCmd();
     String code = cmd.getCode();
     Robo robo = (Robo) op.getOrigem();
     int x = robo.getX();
     int y = robo.getY();
     int dir = 0;
     int indRobo = robo.getInd();
     Terreno terrAtual = mapa.getTerreno(x, y);
     Terreno terrTemp = null;
     Empilhavel resp = new Numero(0);// por default a resposta é falso;
         
     /* If's que verificarão o comando que foi passado como operação e
     o executarão de acordo com a funcionalidade de cada um.         */
 
     switch(Instrucoes.valueOf(code))
     {
       case WALK : 
       case ATK : 
       case COLLECT : 
       case DROP :
       case LOOK:
       case GETROBO:
                try
               {
                 Empilhavel arg = robo.pop();
                 if(arg instanceof Numero) dir = (int)((Numero)arg).getVal();
                 else
                 {
                   System.out.println("Direção não numérica!");
                   System.exit(1);
                 }
                 Direcao direcao = Direcao.toDirecao(dir);
                 switch(direcao)
                 {
                   case UP : terrTemp = mapa.getUp(x, y); break;
                   case UR : terrTemp = mapa.getUpRight(x, y); break;      
                   case DR : terrTemp = mapa.getDownRight(x, y); break;        
                   case DW : terrTemp = mapa.getDown(x, y); break;        
                   case DL : terrTemp = mapa.getDownLeft(x, y); break;        
                   case UL : terrTemp = mapa.getUpLeft(x, y); break; 
                   case HR : terrTemp = mapa.getTerreno(x,y); break;       
                   default : System.out.println("Direçao inválida!");
                 }
               }
               catch(Exception e)
               {
                 //System.out.println("Tentou acesssar posição inexistente:("+x+","+y+")");
                 push(robo, resp);
                 return;
               } 
             
             switch(Instrucoes.valueOf(code))
             {
               case WALK :
                     if(!terrTemp.temRobo() && !terrTemp.eAgua())
                     {
                       if(terrAtual.eRugoso() && !robo.temAtraso()) 
                       {
                         System.out.println("Robo "+indRobo+" tentou se mover, mas está em um terreno rugoso.\n Isso pode levar algum tempo");
                         robo.setAtraso(new Atraso(op,3));
                         robo.push(new Numero(dir)); //Reempilha a direção para uso futuro
                         return; // Se nao tiver robo atrapalhando, empilharemos 1 daqui a tres rodadas, quando ele de fato conseguir se mover, enquanto isso ele fica de boa
                         //resp = new Numero(1); // Conseguiu se mover, mas vai demorar
                       }
                       else
                       {              
                         this.moveRobo(terrAtual, terrTemp);
                         System.out.println("Robo "+indRobo+" se moveu para a posição ("+terrTemp.getX()+","+terrTemp.getY()+")");
                         resp = new Numero(1);
                       }
                     }
                     
                     break; 
 
               case ATK :
                      System.out.println("Robo "+indRobo+" está tentando atacar");
                     if(terrTemp.temRobo())
                     {
                       Robo inim = terrTemp.getRobo();
                       if(inim.getTime() != robo.getTime())
                       {
                         inim.perdeVida(10);
                         System.out.println("Robo "+indRobo+" atacou o robo "+inim.getInd()+"!");
                         if(!inim.temVida())
                         {
                           System.out.println("Robo "+inim.getInd()+" morreu");
                           removeRobo(inim);
                         } 
                         resp = new Numero(1);
                       }
                       else System.out.println("Sem bater nos amiguinhos robô "+indRobo+"!");
                     }
                     else System.out.println("Robo "+indRobo+" do time"+robo.getTime()+" atacou o nada");
                     break;
 
               case COLLECT : 
                     if(terrTemp.eDeposito() && !robo.temCristal())
                     {
                       System.out.println("Robo "+indRobo+" coletou 1 cristal");
                       Deposito dep = terrTemp.toDeposito();
                       if(dep.temCristal())
                       {
                         robo.coletaCristal(dep.popCristal());
                         resp = new Numero(1);
                       }
                       else System.out.println("O depósito esta vazio!");
                     }
                     else
                       if(!terrTemp.eDeposito()) System.out.println("O terreno não é deposito!");
                       else System.out.println("O robo ja tem cristal");
                     break;
               case LOOK :
                     if(terrTemp != null) resp = terrTemp;
                     break;
               case DROP :
                     System.out.println("DROP");
                     if(terrTemp.eBase() && terrTemp.toBase().getTime() != robo.getTime())
                     {
                       dropCristal(robo, terrTemp);
                       System.out.println("Robo "+indRobo+" deixou 1 cristal na base");
                       /*if(terrTemp.toBase().numCristais() >= 1)
                       {
                         System.out.println("Time: "+terrTemp.toBase().getTime().getId()+" perdeu! Todos os robos desse time irão");
                         System.out.println("para o inferno");
                         removeExercito(terrTemp.toBase().getTime().getId());
                         mapa.getTimes().remove(terrTemp.toBase().getTime());
 
                       }*/
                       resp = new Numero(1);        
                     }
                     else
                     {
                       System.out.println("Robo "+indRobo+" tentou deixar cristal na base, mas acabou perdendo ele");
                       perdeCristal(robo);
                     }
                     break;
               case GETROBO :
                     if(terrTemp.temRobo()) resp = terrTemp.getRobo();
                     break;
             }
       break;
       case TIMERB:
             Empilhavel roboAlvo = robo.pop();
             if(roboAlvo instanceof Robo)
               resp = ((Robo)roboAlvo).getTime();
             else
               System.out.println("TIMERB em argumento não robo!!");
       break;
       case GETTIME:
             Empilhavel empId = robo.pop();
             if(empId instanceof Numero)
             {
               int timeId = (int) ((Numero)empId).getVal();
               if(existeTime(timeId)) resp = mapa.getTime(timeId);
             }
             break;
       case MYTIME: robo.getTime(); break;
       default://Talvez outras chamadas venham aqui
     }
     this.push(robo, resp);//empilha a resposta no robo 
 }
 /*
 public boolean addTime(Base base)
 {
   
   int timeId = base.getTime().getId();
   for(ListIterator<Time> it = times.listIterator(); it.hasNext();)
   {
     Time timeTemp = it.next();
     if(timeTemp.getId() == timeId) return false;
   }
   times.add(base.getTime());
   return true;
 }*/
 
 /*
 
 Recebe o número de um possivel time.
 Retorna true caso ele exista nessa instancia da Arena.
 False c.c.
 
 */
 
 public boolean existeTime(int timeId)
 {
   return this.mapa.existeTime(timeId); //Como por enquanto a Arena só tem um mapa, isso basta.
 }
 
 
 /*private Terreno getTerrenoArg(Robo robo)
 {
   Terreno terrTemp = null;
   Empilhavel arg = robo.pop();
   int dir = 0;
   int x = robo.getX();
   int y = robo.getY();
 
 
   if(arg instanceof Numero) dir = (int)((Numero)arg).getVal();
   else
   {
     System.out.println("Direção não numérica!");
     return null;
   }
     Direcao direcao = Direcao.toDirecao(dir);
     switch(direcao)
     {
       case UP : terrTemp = mapa.getUp(x, y); break;
       case UR : terrTemp = mapa.getUpRight(x, y); break;      
       case DR : terrTemp = mapa.getDownRight(x, y); break;        
       case DW : terrTemp = mapa.getDown(x, y); break;        
       case DL : terrTemp = mapa.getDownLeft(x, y); break;        
       case UL : terrTemp = mapa.getUpLeft(x, y); break;        
       default : System.out.println("Direçao inválida!");
     }
   
 
   return terrTemp;
 }
   */
 /* Cria um robo na posição x,y : 
     -Retorna true caso tenha conseguido inserir,
     -Retorna false c.c.                            
 */
 private boolean insereRobo(Robo rb)
 {
   /* Tenta colocar o robo no mapa. Caso já 
   exista um robo, a função irá retornar 0*/
   if(mapa.putRobo(rb))
   {
     this.robos.add(rb);
     return true;
   }
   return false;
 }
 
 public void insereExercito(Programa[] programas, int timeId)
 {
 
   if(!existeTime(timeId)) throw new IllegalArgumentException();
   Time time = mapa.getTime(timeId);
   int tam = programas.length;
   
  int maxX = this.mapa.largura() -1;
  int maxY = this.mapa.altura() -1;
 
 
   for(int i = 0; i < tam; i++)
   {
     Posicao pos = new Posicao(0,0);
    /* Robo rb = new Robo(this,posicoes[i], time);*/
     do pos.randXY(maxX,maxY);
     while(mapa.getTerreno(pos).eAgua());
     
     Robo rb = new Robo(this, pos, time);
 
     rb.carregaPrograma(programas[i]);
 
     if( insereRobo(rb) )
       System.out.println("Robo "+rb.getInd()+" inserido com sucesso em "+rb.getX()+","+rb.getY()+" ");
     else
       System.out.println("Falha ao tentar inserir o robo "+i);
   }
 
 }  
 
 /* Remove todos os robos que pertencem ao time passado */
 public void removeExercito(int time)
 {
   ArrayList<Robo> robosMortos = new ArrayList<Robo>();
   Iterator<Robo> it = robos.iterator();
   while (it.hasNext()) 
   {
     Robo rb = it.next();
     if (rb.getTime().getId() == time)
     {
       removeRobo(rb);
       robosMortos.add(rb);  
     }    
   }
 
   robos.removeAll(robosMortos);
 }
 
  /* Recebe um robo, que será retirado da lista de robos
  ativos e que será removido do mapa */
  private void removeRobo(Robo rb)
  {
   Posicao pos = rb.getPos();
   mapa.getTerreno(pos).removeRobo();
 }
 
 private void moveRobo(Terreno origem, Terreno destino)
 {
   Robo robo = origem.removeRobo();
   destino.putRobo(robo);
   robo.setPos(destino.getPos());
 }
 
 private boolean podeColetar(Robo robo, Terreno destino)
 {
   return (!robo.temCristal() && destino.eDeposito() && destino.toDeposito().temCristal());
 }
 
 private void dropCristal(Robo robo, Terreno destino)
 {
   destino.toBase().putCristal(robo.dropCristal());
 }
 
 private void perdeCristal(Robo robo)
 {
   Cristal cris = robo.dropCristal();
   Terreno depTemp = mapa.getTerreno(cris.getX(), cris.getY());
   depTemp.toDeposito().putCristal(cris);
 }
 
 private void push(Robo robo, Empilhavel resp)
 {
   robo.push(resp);
 }
 
 private Empilhavel pop(Robo robo)
 {
   return robo.pop();
 }
 }
 
 
