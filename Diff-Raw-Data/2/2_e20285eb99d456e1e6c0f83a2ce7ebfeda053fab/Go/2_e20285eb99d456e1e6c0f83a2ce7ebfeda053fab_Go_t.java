 package org.beroot.android.goengine;
 
 /**
  * @author Nicolas
  * 
  */
 public class Go
 {
 
   // ------------------------------------------------------------------------
   // Constantes
   // ------------------------------------------------------------------------
   public static final byte GRAY = 9;
   public static final byte EMPTY = 0;
   public static final byte BLACK = 1;
   public static final byte WHITE = 2;
   public static final byte PASS_MOVE = 0;
 
   /**
    * Taille du goban 9x9 - 13x13 - 19x19
    */
   private final int maxBoard;
 
   /**
    * Facteur de décallage
    */
   private final int NS;
 
   /**
    * Taille du tableau stockant le goban
    */
   private final int boardSize;
 
   /**
    * Première borne correspondant à une pierre
    */
   public final int boardMin;
 
   /**
    * Dernière borne correspondant à une pierre
    */
   public final int boardMax;
 
   /**
    * Coordinates for the eight directions, ordered :
    * south, west, north, east, southwest, northwest, northeast, southeast.
    */
   private int[] delta;
 
   // ------------------------------------------------------------------------
   // Données de jeu
   // ------------------------------------------------------------------------
   /**
    * Représentation interne du goban
    */
   public int[] _board;
 
   /**
    * Tableau qui pour une position donnée sur le goban référence la chaine associée
    */
   private int[] _stringNumber;
 
   /**
    * Liste de toutes les chaines
    */
   private StringData[] _strings;
 
   /**
    * Numéro de la prochaine chaine
    */
   private int _nextString;
 
   /**
    * Incrément des marques des chaines
    */
   private int _markString;
 
   /**
    * Incrément des marques des libertées
    */
   private int _markLiberty;
 
   /**
    * Tableau de marquage des libertés
    */
   private int[] _markLiberties;
 
   /**
    * Nombre de pierres blanches capturées
    */
   private int _capturedWhite;
 
   /**
    * Nombre de pierres noires capturées
    */
   private int _capturedBlack;
 
   private int _lastKoPos;
 
   // ------------------------------------------------------------------------
   // Méthodes utilitaires
   // ------------------------------------------------------------------------
   /**
    * 
    * @param pos
    * @return
    */
   public int south(int pos)
   {
     return ((pos) + NS);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public int west(int pos)
   {
     return ((pos) - 1);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public int north(int pos)
   {
     return ((pos) - NS);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public int east(int pos)
   {
     return ((pos) + 1);
   }
 
   /**
    * 
    * @param i
    * @param j
    * @return
    */
   public int pos(int i, int j)
   {
     return ((maxBoard + 2) + (i) * (maxBoard + 1) + (j));
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public int I(int pos)
   {
     return ((pos) / (maxBoard + 1) - 1);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public int J(int pos)
   {
     return ((pos) % (maxBoard + 1) - 1);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public boolean onBoard(int pos)
   {
     return _board[pos] != GRAY;
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   private boolean onBoard1(int pos)
   {
     return pos < boardSize && _board[pos] != GRAY;
   }
 
   /**
    * 
    * @param i
    * @param j
    * @return
    */
   private boolean onBoard2(int i, int j)
   {
     return !(i < 0 || j < 0 || pos(i, j) > boardMax);
   }
 
   /**
    * 
    * @param color
    * @return
    */
   private int otherColor(int color)
   {
     return (BLACK + WHITE) - color;
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   private int liberties(int pos)
   {
     return _strings[_stringNumber[pos]].libertyCount;
   }
 
   /**
    * 
    * @param pos
    */
   private void markString(int pos)
   {
     _strings[_stringNumber[pos]].mark = _markString;
   }
 
   /**
    * 
    * @param pos
    * @param color
    * @return
    */
   private boolean unmarkedColorString(int pos, int color)
   {
     return _board[pos] == color && _strings[_stringNumber[pos]].mark != _markString;
   }
 
   /**
    * 
    * @param s
    * @param pos
    */
   private void addNeighbor(int s, int pos)
   {
     _strings[s].addNeighbor(_stringNumber[pos]);
   }
 
   /**
    * 
    * @param s
    * @param pos
    */
   private void addLiberty(int s, int pos)
   {
     _strings[s].addLiberty(pos);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   private boolean unmarkedLiberty(int pos)
   {
     return _board[pos] == EMPTY && _markLiberties[pos] != _markLiberty;
   }
 
   /**
    * 
    * @param pos
    */
   private void markLiberty(int pos)
   {
     _markLiberties[pos] = _markLiberty;
   }
 
   /**
    * 
    * @param s
    * @param pos
    */
   private void addAndMarkLiberty(int s, int pos)
   {
     addLiberty(s, pos);
     markLiberty(pos);
   }
 
   /**
    * 
    * @param pos
    * @param color
    * @return
    */
   private boolean hasNeighbor(int pos, int color)
   {
     return (_board[south(pos)] == color || _board[west(pos)] == color || _board[north(pos)] == color || _board[east(pos)] == color);
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public boolean isLastKoPos(int pos)
   {
     return pos == _lastKoPos;
   }
 
   /**
 	 * 
 	 *
 	 */
   public void display()
   {
     for (int i = 0; i < boardSize; i++)
     {
       if (i > 0 && i % (maxBoard + 1) == 0)
       {
         System.out.println("");
       }
       System.out.print(_board[i] + " ");
     }
     System.out.println("");
     System.out.println("");
   }
 
   /**
 	 * 
 	 *
 	 */
   public void display2()
   {
     int i;
     for (i = boardMin; i < boardMax; i++)
     {
       if (i > 0 && (i % (maxBoard + 1) == 0))
       {
         System.out.println("");
       }
       if (onBoard(i))
       {
         System.out.print(_board[i] + " ");
       }
     }
     System.out.println("");
   }
 
   // ------------------------------------------------------------------------
   // Constructeur
   // ------------------------------------------------------------------------
   /**
    * 
    * @param size
    */
   public Go(int size)
   {
     maxBoard = size;
 
     // calcul des constantes
     NS = maxBoard + 1;
     int[] deltaTemp = { NS, -1, -NS, 1, NS - 1, -NS - 1, -NS + 1, NS + 1 };
     delta = deltaTemp;
     boardSize = (maxBoard + 2) * (maxBoard + 1) + 1;
     boardMin = maxBoard + 2;
     boardMax = (maxBoard + 1) * (maxBoard + 1);
 
     // Initialisation des structures
     _board = new int[boardSize];
     initInternals();
 
     // Ajout des bornes sur le goban
     for (int k = 0; k < boardSize; k++)
     {
       if (!onBoard2(I(k), J(k)))
       {
         _board[k] = GRAY;
       }
     }
   }
 
   /**
 	 * 
 	 */
   private void initInternals()
   {
     _strings = new StringData[boardSize];
     _stringNumber = new int[boardSize];
     _markLiberties = new int[boardSize];
     _nextString = 0;
     _markString = 0;
     _markLiberty = 0;
     _capturedWhite = 0;
     _capturedBlack = 0;
     _lastKoPos = 0;
 
     for (int pos = boardMin; pos < boardMax; pos++)
     {
       _stringNumber[pos] = -1;
     }
   }
 
   // ------------------------------------------------------------------------
   // Logique de jeu en coup par coup
   // ------------------------------------------------------------------------
   /**
    * Joue un coup
    * 
    * @param x Coordonnée x
    * @param y Coordonnée y
    * @param color Couleur de la pierre
    * @return true / false pour indiquer si le coup a été joué ou non
    */
   public boolean play(int x, int y, byte color)
   {
     int pos = pos(x, y);
 
     // 1 - Vérification si le coup est jouable
     if (!isLegal(pos, color))
     {
       return false;
     }
 
     int copains = 0;
     int s = 0;
     int capturedStones = 0;
 
     _lastKoPos = 0;
     _board[pos] = color;
     _stringNumber[pos] = -1;
     _markString++;
 
     // 2 - Parcours des 4 directions cardinales
     int direction;
     for (int k = 0; k < 4; k++)
     {
       direction = pos + delta[k];
 
       // Si il y a une pierre amie d'une chaine non visitée
       if (unmarkedColorString(direction, color))
       {
         copains++;
         s = _stringNumber[direction];
         markString(direction);
       }
       // Si il y a une pierre adverse d'une chaine non visitée
       else if (unmarkedColorString(direction, otherColor(color)))
       {
         // Si elle a au moins 2 libertées on met à jour la chaine et on la marque
         if (liberties(direction) > 1)
         {
           updateLiberties(_stringNumber[direction]);
           markString(direction);
         }
         // Elle n'a plus qu'une libertée, la chaine est capturée
         else
         {
           capturedStones += removeString(_stringNumber[direction]);
         }
       }
     }
 
     // 3 - choix de la stratégie en fonction des voisins
     // 3.1 - Pierre isolée, on cré une nouvelle chaine
     if (copains == 0)
     {
       createNewString(pos);
     }
     // 3.2 - Une seule chaine amie voisine, on intègre la pierre à cette chaine
     else if (copains == 1)
     {
       extendNeighborString(pos, s);
       return true; // pas ko
     }
     // 3.3 - Sinon on fusionne les chaines amies adjacentes
     else
     {
       assimilateNeighborStrings(pos);
       return true; // pas ko
     }
 
     // 4 - Gestion du ko
     s = _stringNumber[pos];
     if (_strings[s].libertyCount == 1 && _strings[s].size == 1 && capturedStones == 1)
     {
       _lastKoPos = _strings[s].liberties[0];
     }
 
     return true;
   }
 
   /**
    * @param pos
    */
   private void createNewString(int pos)
   {
     int color = _board[pos];
     int s = _nextString++;
 
     _stringNumber[pos] = s;
     _strings[s] = new StringData(boardSize, pos, color);
     _markString++;
 
     // Parcours des 4 directions cardinales
     int direction;
     for (int k = 0; k < 4; k++)
     {
       direction = pos + delta[k];
       // si l'intersection est vide, on a ajoute une liberté à la chaine
       if (_board[direction] == EMPTY)
       {
         addLiberty(s, direction);
       }
       // Si l'intersection correspond à une chaine adverse non marquée
       else if (unmarkedColorString(direction, otherColor(color)))
       {
         // Ajout des voisins dans les 2 sens et marquage de la chaine
         // La mise à jour des libertées a déjà été faite dans la méthode play()
         int s2 = _stringNumber[direction];
         addNeighbor(s, direction);
         addNeighbor(s2, pos);
         markString(direction);
       }
     }
   }
 
   /**
    * @param pos
    * @param string
    */
   private void extendNeighborString(int pos, int s)
   {
     // Intégration de la pierre à sa chaine
     _strings[s].addStone(pos);
     _stringNumber[pos] = s;
 
     // Mise à jour des libertées de la chaine
     updateLiberties(s);
 
     // Mise à jour des chaines adverses
     _markString++;
     int color = _board[pos];
     int direction;
     for (int k = 0; k < 4; k++)
     {
       direction = pos + delta[k];
       if (unmarkedColorString(direction, otherColor(color)))
       {
         updateLiberties(_stringNumber[direction]);
         markString(direction);
         int s2 = _stringNumber[direction];
         addNeighbor(s, direction);
         addNeighbor(s2, pos);
       }
     }
   }
 
   /**
    * 
    * @param pos
    */
   private void assimilateNeighborStrings(int pos)
   {
     int color = _board[pos];
     int s = _nextString++;
 
     _stringNumber[pos] = s;
     _strings[s] = new StringData(boardSize, pos, color);
     _markString++;
     markString(pos);
 
     int direction;
     for (int k = 0; k < 4; k++)
     {
       direction = pos + delta[k];
       if (unmarkedLiberty(direction))
       {
         addAndMarkLiberty(s, direction);
       }
       else if (unmarkedColorString(direction, otherColor(color)))
       {
         int s2 = _stringNumber[direction];
         addNeighbor(s, direction);
         addNeighbor(s2, pos);
        markString(direction);
       }
       else if (unmarkedColorString(direction, color))
       {
         assimilateString(s, direction);
       }
     }
   }
 
   /**
    * @param s
    * @param i
    */
   private void assimilateString(int s, int pos2)
   {
     int s2 = _stringNumber[pos2];
     int i;
 
     // ajout des pierres de s2 dans s
     for (i = 0; i < _strings[s2].size; i++)
     {
       int pos = _strings[s2].stones[i];
       _stringNumber[pos] = s;
       _strings[s].addStone(pos);
     }
 
     // Recalcul du nombre de libertées
     updateLiberties(s);
 
     // Gestion des voisins
     int n;
     for (i = 0; i < _strings[s2].neighborCount; i++)
     {
       n = _strings[s2].neighbors[i];
       _strings[n].removeNeighbor(s2);
       if (_strings[n].mark != _markString)
       {
         _strings[n].addNeighbor(s);
         _strings[s].addNeighbor(n);
         _strings[n].mark = _markString;
       }
     }
 
     // Suppression de la chaine qui vient d'être intégrée
     _strings[s2] = null;
   }
 
   /**
    * 
    * @param s
    * @return
    */
   private int removeString(int s)
   {
     int size = _strings[s].size;
 
     // Suppression des pierres du goban
     int i;
     int pos;
     for (i = 0; i < size; i++)
     {
       pos = _strings[s].stones[i];
       _board[pos] = EMPTY;
     }
 
     // Mise à jour des chaines voisines
     for (i = 0; i < _strings[s].neighborCount; i++)
     {
       updateLiberties(_strings[s].neighbors[i]);
       _strings[_strings[s].neighbors[i]].removeNeighbor(s);
     }
 
     // Mise à jour des compteurs de captures
     if (_strings[s].color == WHITE)
     {
       _capturedWhite += size;
     }
     else
     {
       _capturedBlack += size;
     }
 
     // Suppression de la structure de la chaine
     _strings[s] = null;
 
     return size;
   }
 
   /**
    * 
    * @param s
    */
   private void updateLiberties(int s)
   {
     // RAZ des libertées de la chaine
     _strings[s].resetLiberties();
     _markLiberty++;
     int pos2;
 
     // Pour chaque pierre de la chaine :
     // on va chercher les libertés dans les 4 directions
     // chaque libertée ajoutée est marquée
     for (int i = 0; i < _strings[s].size; i++)
     {
       pos2 = _strings[s].stones[i];
       if (unmarkedLiberty(south(pos2)))
       {
         addAndMarkLiberty(s, south(pos2));
       }
       if (unmarkedLiberty(north(pos2)))
       {
         addAndMarkLiberty(s, north(pos2));
       }
       if (unmarkedLiberty(east(pos2)))
       {
         addAndMarkLiberty(s, east(pos2));
       }
       if (unmarkedLiberty(west(pos2)))
       {
         addAndMarkLiberty(s, west(pos2));
       }
     }
   }
 
   /**
    * 
    * @param pos
    * @return
    */
   public boolean isLegal(int pos, int color)
   {
     if (pos == PASS_MOVE)
     {
       return true;
     }
 
     if (!onBoard1(pos))
     {
       return false;
     }
 
     if (_board[pos] != EMPTY)
     {
       return false;
     }
 
     if (pos == _lastKoPos && hasNeighbor(pos, otherColor(color)))
     {
       return false;
     }
     // TODO qu'on est pas en suicide 
 
     return true;
   }
 
   // ------------------------------------------------------------------------
   // Logique de chargement d'une position complète
   // ------------------------------------------------------------------------
   /**
 	 * 
 	 */
   public void newPosition()
   {
     // On ne garde que les pierres posées sur le goban
     initInternals();
 
     // Recherche des chaines existantes
     for (int pos = boardMin; pos < boardMax; pos++)
     {
       if (onBoard(pos) && (_board[pos] == WHITE || _board[pos] == BLACK) && _stringNumber[pos] == -1)
       {
         _stringNumber[pos] = _nextString;
         _strings[_nextString] = new StringData(boardSize, pos, _board[pos]);
         propagateString(pos, _nextString);
         _nextString++;
       }
     }
 
     // Recherche des libertés et des voisins de chaque chaine trouvée
     for (int s = 0; s < _nextString; s++)
     {
       findLibertiesAndNeighbors(s);
     }
   }
 
   /**
    * @param pos
    * @param s
    */
   private void propagateString(int pos, int s)
   {
     // Recherche à étendre la chaine dans les 4 directions cardinales
     for (int k = 0; k < 4; k++)
     {
       int direction = pos + delta[k];
 
       // Si il y a une pierre de la même couleur qui n'est pas encore ajoutée
       if (onBoard(direction) && _board[direction] == _board[pos] && _stringNumber[direction] == -1)
       {
         _stringNumber[direction] = s;
         _strings[s].addStone(direction);
         propagateString(direction, s);
       }
     }
   }
 
   /**
    * @param s
    */
   private void findLibertiesAndNeighbors(int s)
   {
     _markLiberty++;
     _markString++;
 
     int pos;
     int direction;
 
     // Pour chaque pierre de la chaine
     for (int i = 0; i < _strings[s].size; i++)
     {
       pos = _strings[s].stones[i];
 
       // Parcours des 4 directions cardinales
       for (int k = 0; k < 4; k++)
       {
         direction = pos + delta[k];
 
         // A la recherche d'une libertée non marquée
         if (unmarkedLiberty(direction))
         {
           addAndMarkLiberty(s, direction);
         }
         // ou d'une chaine adverse non marquée
         else if (unmarkedColorString(direction, otherColor(_strings[s].color)))
         {
           addNeighbor(s, direction);
           markString(direction);
         }
       }
     }
   }
 
   public void addStone(int pos, int color)
   {
     _board[pos] = color;
   }
 }
