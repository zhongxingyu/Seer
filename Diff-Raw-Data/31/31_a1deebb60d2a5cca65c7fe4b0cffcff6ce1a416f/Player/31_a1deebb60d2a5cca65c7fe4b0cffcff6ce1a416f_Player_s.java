 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author Michal
  */
 public class Player 
 {
     public static final int NUMBER_OF_L_SHAPE_FLIP_POSIBILITIES = 2;
     public static final int NUMBER_OF_L_SHAPE_ROTATE_POSIBILITIES = 4;
     
     public static final int NUMBER_OF_DOT_SHAPE_ROTATE_POSIBILITIES = 0;
     public static final int NUMBER_OF_DOT_SHAPE_FLIP_POSIBILITIES = 0;
     
    private Pawn lShapePawn, dotShapePawn;
     final public int _id;
     
     public Player(int id)
     {
         dotShapePawn    = new DotShapePawn(id);
         lShapePawn      = new LShapePawn(id);
         
         lShapePawn.PrintAllConfigurations();
        //dotShapePawn.PrintAllConfigurations();
         
         _id = id;

     }
     
     public void go()
     {
         //
     }
 }
 
 // 
 
 
 abstract class Pawn
 {
     public final static int SMALL_BOARD_SIZE = 3;
     
     protected final int     _id;
     protected List<int[][]> listOfPawnConfigurations;
     protected int           configurationInUse;
     
     public Pawn(int id)
     {
         listOfPawnConfigurations = new ArrayList<>();
         listOfPawnConfigurations.add(new int[SMALL_BOARD_SIZE][SMALL_BOARD_SIZE]);
         
         configurationInUse = 0; // domyślna wartość to 0 
         _id = id;
 
         InitRotateAndFlipPawnPosibilities(id);
     }
     
     public void PrintAllConfigurations()
     {
         for(int i=0; i < listOfPawnConfigurations.size(); ++i)
         {
             System.out.println("Conf" + i + "= ");
             Print(listOfPawnConfigurations.get(i));
             System.out.println();
         }
     }
     
     public void Print(int[][] matrixToPrint)
     {
         for ( int[] row : matrixToPrint ) 
         {
             System.out.println(Arrays.toString(row));
         }
     }
     
     public int[][] RotateCW(int[][] boardToRotate)
     {
         // if jesli więćej nic 4 obroty?
         
         int[][] tmp = new int[SMALL_BOARD_SIZE][SMALL_BOARD_SIZE];
         
         for (int i = 0; i < SMALL_BOARD_SIZE; i++) 
         {
             for (int j = 0; j < SMALL_BOARD_SIZE; j++) 
             {
                 tmp[i][j] = boardToRotate[SMALL_BOARD_SIZE-j-1][i];
             }
         }
         return tmp;
     }
     
     public int[][] FlipHorizontal(int[][] boardToFlip)
     {
         int[][] tmp = new int[SMALL_BOARD_SIZE][SMALL_BOARD_SIZE];
         for(int i = 0; i < SMALL_BOARD_SIZE; ++i)
         {
             for(int j = 0; j < SMALL_BOARD_SIZE; ++j)
             {
                 tmp[SMALL_BOARD_SIZE-i-1][j] = boardToFlip[i][j];
             }
         }        
         return tmp;
     }
     
     public int[][] FlipVertical(int[][] boardToFlip)
     {
         int[][] tmp = new int[SMALL_BOARD_SIZE][SMALL_BOARD_SIZE];
         for(int i = 0; i < SMALL_BOARD_SIZE; ++i)
         {
             for(int j = 0; j < SMALL_BOARD_SIZE; ++j)
             {
                 tmp[i][SMALL_BOARD_SIZE-j-1] = boardToFlip[i][j];
             }
         }        
         return tmp;
     }
     
     public void FillListOfPawnConfigurations(int start, int stop)
     {
         for(int i = start; i<stop; ++i)
         {
             // 1. pobierany indeks ostatniego układu
             // 2. pobieranie macierzy pod znalezionych indeksem
             // 3. obrót macierzy pod znalezionym indeksem
             // 4. dodanie bitmapy / tablicy 3x3 z nowym ułożeniem pionka to możliwych konfiguracji
             listOfPawnConfigurations.add( RotateCW(listOfPawnConfigurations.get(i)) );   
         }
     }
     
     public int GetAmoutOfRotatePosibilities()
     {
         return listOfPawnConfigurations.size();
     }
     
     abstract public void InitRotateAndFlipPawnPosibilities(int id);
 }
 
 
 class DotShapePawn extends Pawn
 {
     public DotShapePawn(int id)
     {
         super(id);
     }
     
     @Override
     public void InitRotateAndFlipPawnPosibilities(int id)
     {
         // wprowadzenie początkowych danych, dot shape
         listOfPawnConfigurations.get(configurationInUse)[1][1] = id;
         
         // tutaj nie jest potrzebne obrananie
         // zatem nie m sensu generowac możliwych pozycji
         try
         {
             if(listOfPawnConfigurations.size() != 1)
                 throw new Exception("[Błąd], możliwości obrotu w dotShape != 1");
         }
         catch(Exception ex)
         {
             System.err.println(ex.getMessage());
         }
     }
 }
 
 class LShapePawn extends Pawn
 {
     public LShapePawn(int id)
     {
         super(id);
     }
     
     @Override
     public void InitRotateAndFlipPawnPosibilities(int id) 
     {
         // wprowadzenie początkowych danych, L shape
         listOfPawnConfigurations.get(configurationInUse)[0][1]= id;
         listOfPawnConfigurations.get(configurationInUse)[1][1]= id;
         listOfPawnConfigurations.get(configurationInUse)[2][1]= id;
         listOfPawnConfigurations.get(configurationInUse)[2][0]= id;
         
         
         {   // tworzenie listy wszystkich możliwych układów ułożenia pionka (w tablicy 3x3)
             FillListOfPawnConfigurations(0, Player.NUMBER_OF_L_SHAPE_ROTATE_POSIBILITIES-1);
 
             // 1. pobierany indeks pierwszego w układzie
             // 2. pobieranie macierzy pod znalezionych indeksem
             // 3. odbicie w pionie macierzy pod znalezionym indeksem
             // 4. dodanie bitmapy / tablicy 3x3 z nowym ułożeniem pionka to możliwych konfiguracji
             listOfPawnConfigurations.add( FlipVertical(listOfPawnConfigurations.get(0)) );
 
             FillListOfPawnConfigurations(listOfPawnConfigurations.size() - 1, Player.NUMBER_OF_L_SHAPE_ROTATE_POSIBILITIES-1 + listOfPawnConfigurations.size() - 1);
         }
     } 
 }
