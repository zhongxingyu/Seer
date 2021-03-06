 //-----------------------------------------------------------------------------
 // $Id$
 // $Source$
 //-----------------------------------------------------------------------------
 
 package go;
 
 import java.util.*;
 
 //-----------------------------------------------------------------------------
 
 class MoveRecord
 {
     public MoveRecord(Color oldToMove, Move m, Color old, Vector killed,
                       Vector suicide)
     {
         m_old = old;
         m_oldToMove = oldToMove;
         m_move = m;
         m_killed = killed;
         m_suicide = suicide;
     }
 
     public Move getMove()
     {
         return m_move;
     }
 
     public Color getOldColor()
     {
         return m_old;
     }
 
     public Color getOldToMove()
     {
         return m_oldToMove;
     }
 
     public Vector getKilled()
     {
         return m_killed;
     }
 
     public Vector getSuicide()
     {
         return m_suicide;
     }
 
     private Color m_old;
 
     private Color m_oldToMove;
 
     private Move m_move;
 
     private Vector m_killed;
 
     private Vector m_suicide;
 }
 
 //-----------------------------------------------------------------------------
 
 public class Board
 {
     public static final int RULES_JAPANESE = 0;
 
     public static final int RULES_CHINESE = 1;
 
     public Board(int boardSize)
     {
         initSize(boardSize);
     }
 
     public boolean bothPassed()
     {
         if (m_moveNumber < 2)
             return false;
         return (getInternalMove(m_moveNumber - 1).getPoint() == null
                 && getInternalMove(m_moveNumber - 2).getPoint() == null);
     }
 
     public void calcScore()
     {
         assert(isMarkCleared());
         boolean allEmpty = true;
         for (int i = 0; i < m_allPoints.length; ++i)
         {
             Point p = m_allPoints[i]; 
             Color c = getColor(p);
             if (c != Color.EMPTY)
             {
                 allEmpty = false;
                 if (! getDead(p))
                     setScore(p, c);
             }
             else
                 setScore(p, Color.EMPTY);
         }
         if (allEmpty)
             return;
         Vector territory = new Vector(getNumberPoints());
         for (int i = 0; i < m_allPoints.length; ++i)
         {
             Point p = m_allPoints[i];
             if (! getMark(p))
             {
                 territory.clear();
                 if (isTerritory(p, territory, Color.BLACK))
                 {
                     for (int j = 0; j < territory.size(); ++j)
                         setScore((Point)territory.get(j), Color.BLACK);
                 }
                 else
                 {
                     setMark(territory, false);
                     if (isTerritory(p, territory, Color.WHITE))
                     {
                         for (int j = 0; j < territory.size(); ++j)
                             setScore((Point)territory.get(j), Color.WHITE);
                     }
                     else
                     {
                         setMark(territory, false);
                     }
                 }
             }
         }
         clearMark();
     }
 
     public Vector getAdjacentPoints(Point p)
     {
         Vector result = new Vector(4);
         int x = p.getX();
         int y = p.getY();
         if (x > 0)
             result.add(m_point[x - 1][y]);
         if (x < m_size - 1)
             result.add(m_point[x + 1][y]);
         if (y > 0)
             result.add(m_point[x][y - 1]);
         if (y < m_size - 1)
             result.add(m_point[x][y + 1]);
         return result;
     }
 
     /** @see setDead. */
     public boolean getDead(Point p)
     {
         return m_dead[p.getX()][p.getY()];
     }
 
     public Color getScore(Point p)
     {
         return m_score[p.getX()][p.getY()];
     }
 
     public int getSize()
     {
         return m_size;
     }
 
     public int getCapturedB()
     {
         return m_capturedB;
     }
 
     public int getCapturedW()
     {
         return m_capturedW;
     }
 
     public Color getColor(Point p)
     {
         return m_color[p.getX()][p.getY()];
     }
 
     public Vector getHandicapStones(int n)
     {
         Vector result = new Vector(9);
         if (n == 0)
             return result;
         if (m_handicapLine1 < 0)
             return null;
         if (n > 4 && m_handicapLine2 < 0)
             return null;
         if (n >= 1)
             result.add(new Point(m_handicapLine1, m_handicapLine1));
         if (n >= 2)
             result.add(new Point(m_handicapLine3, m_handicapLine3));
         if (n >= 3)
             result.add(new Point(m_handicapLine1, m_handicapLine3));
         if (n >= 4)
             result.add(new Point(m_handicapLine3, m_handicapLine1));
         if (n >= 5)
             if (n % 2 != 0)
             {
                 result.add(new Point(m_handicapLine2, m_handicapLine2));
                 --n;
             }
         if (n >= 5)
             result.add(new Point(m_handicapLine1, m_handicapLine2));
         if (n >= 6)
             result.add(new Point(m_handicapLine3, m_handicapLine2));
         if (n >= 7)
             result.add(new Point(m_handicapLine2, m_handicapLine1));
         if (n >= 8)
             result.add(new Point(m_handicapLine2, m_handicapLine3));
         return result;
     }
 
     public float getKomi()
     {
         return m_komi;
     }
 
     public Move getMove(int i)
     {
         return ((MoveRecord)m_moves.get(i + m_setupNumber)).getMove();
     }
 
     public Point getPoint(int i)
     {
         return m_allPoints[i];
     }
 
     public Point getPoint(int x, int y)
     {
         return m_point[x][y];
     }
 
     public Move getInternalMove(int i)
     {
         return ((MoveRecord)m_moves.get(i)).getMove();
     }
 
     public int getMoveNumber()
     {
         return m_moveNumber - m_setupNumber;
     }
 
     public int getNumberPoints()
     {
         return m_allPoints.length;
     }
 
     public int getNumberSavedMoves()
     {
         return m_moves.size() - m_setupNumber;
     }
 
     public int getRules()
     {
         return m_rules;
     }
 
     public Vector getSetupStonesBlack()
     {
         return m_setupStonesBlack;
     }
 
     public Vector getSetupStonesWhite()
     {
         return m_setupStonesWhite;
     }
 
     public void getStones(Point p, Color color, Vector stones)
     {
         assert(isMarkCleared());
         findStones(p, color, stones);
         setMark(stones, false);
         assert(isMarkCleared());
     }
 
     public Color getToMove()
     {
         return m_toMove;
     }
 
     public void initSize(int size)
     {
         m_size = size;
         m_color = new Color[m_size][m_size];
         m_mark = new boolean[m_size][m_size];
         m_dead = new boolean[m_size][m_size];
         m_score = new Color[m_size][m_size];
         m_point = new Point[m_size][m_size];
         m_capturedB = 0;
         m_capturedW = 0;
         initAllPoints();
         m_handicapLine1 = -1;
         m_handicapLine2 = -1;
         m_handicapLine3 = -1;
         if (m_size >= 13)
         {
             m_handicapLine1 = 3;
             m_handicapLine3 = m_size - 4;
         }
         else if (m_size >= 8)
         {
             m_handicapLine1 = 2;
             m_handicapLine3 = m_size - 3;
         }
         if (m_size >= 11 && m_size % 2 != 0)
             m_handicapLine2 = m_size / 2;
         newGame();
     }
 
     public boolean isHandicap(Point p)
     {
         int x = p.getX();
         int y = p.getY();
         return (isOnHandicapLine(x) && isOnHandicapLine(y));
     }
 
     public boolean isModified()
     {
         return (m_moves.size() > 0 || m_setupNumber > 0);
     }
 
     public void newGame()
     {
         for (int i = 0; i < m_allPoints.length; ++i)
             setColor(m_allPoints[i], Color.EMPTY);
         m_moves.clear();
         m_moveNumber = 0;
         m_setupNumber = 0;
         m_capturedB = 0;
         m_capturedW = 0;
         m_toMove = Color.BLACK;
        m_setupStonesBlack.clear();
        m_setupStonesWhite.clear();
     }
 
     public void play(Move m)
     {
         Point p = m.getPoint();
         Color color = m.getColor();
         Color otherColor = color.otherColor();
         Vector killed = new Vector();
         Vector suicide = new Vector();
         Color old = Color.EMPTY;
         if (p != null)
         {
             old = getColor(p);
             setColor(p, color);
             if (color != Color.EMPTY)
             {
                 Vector adj = getAdjacentPoints(p);
                 for (int i = 0; i < adj.size(); ++i)
                     checkKill((Point)(adj.get(i)), otherColor, killed);
                 checkKill(p, color, suicide);
                 if (color == Color.BLACK)
                 {
                     m_capturedB += suicide.size();
                     m_capturedW += killed.size();
                 }
                 else
                 {
                     m_capturedW += suicide.size();
                     m_capturedB += killed.size();
                 }
             }
         }
         if (m_moveNumber == m_moves.size()
              || ! m.equals(getInternalMove(m_moveNumber)))
         {
             m_moves.setSize(m_moveNumber);
             m_moves.add(new MoveRecord(m_toMove, m, old, killed, suicide));
         }
         ++m_moveNumber;
         m_toMove = otherColor;        
     }
 
     public void scoreBegin(Point[] isDeadStone)
     {
         for (int i = 0; i < m_allPoints.length; ++i)
             setDead(m_allPoints[i], false);
         if (isDeadStone != null)
             for (int i = 0; i < isDeadStone.length; ++i)
                 setDead(isDeadStone[i], true);
         calcScore();
     }
 
     public void scoreSetDead(Point p)
     {
         Color c = getColor(p);
         if (c == Color.EMPTY)
             return;
         Vector stones = new Vector(getNumberPoints());
         getStones(p, c, stones);
         boolean dead = ! getDead((Point)(stones.get(0)));
         for (int i = 0; i < stones.size(); ++i)
         {
             Point stone = (Point)stones.get(i);
             setDead(stone, dead);
         }
         calcScore();
     }
 
     public Score scoreGet()
     {
         Score s = new Score();
         s.m_rules = m_rules;        
         s.m_komi = getKomi();        
         s.m_capturedBlack = m_capturedB;
         s.m_capturedWhite = m_capturedW;
         int areaDiff = 0;
         int territoryDiff = 0;
         for (int i = 0; i < m_allPoints.length; ++i)
         {
             Point p = m_allPoints[i];
             Color c = getColor(p);
             Color sc = getScore(p);
             if (sc == Color.BLACK)
             {
                 ++s.m_areaBlack;
                 ++areaDiff;
             }
             else if (sc == Color.WHITE)
             {
                 ++s.m_areaWhite;
                 --areaDiff;
             }
             if (c == Color.EMPTY)
             {
                 if (sc == Color.BLACK)
                 {
                     ++s.m_territoryBlack;
                     ++territoryDiff;
                 }
                 else if (sc == Color.WHITE)
                 {
                     ++s.m_territoryWhite;
                     --territoryDiff;
                 }
             }
             if (c == Color.BLACK && sc == Color.WHITE)
             {
                 ++s.m_capturedBlack;
                 ++s.m_territoryWhite;
                 --territoryDiff;
             }
             if (c == Color.WHITE && sc == Color.BLACK)
             {
                 ++s.m_capturedWhite;
                 ++s.m_territoryBlack;
                 ++territoryDiff;
             }
         }
         s.m_resultChinese = areaDiff - m_komi;
         s.m_resultJapanese =
             s.m_capturedWhite - s.m_capturedBlack + territoryDiff - m_komi;
         if (m_rules == RULES_JAPANESE)
             s.m_result = s.m_resultJapanese;
         else
         {
             assert(m_rules == RULES_CHINESE);
             s.m_result = s.m_resultChinese;
         }
         return s;
     }
 
     /** Mark point as dead for scoring. */
     public void setDead(Point p, boolean value)
     {
         m_dead[p.getX()][p.getY()] = value;
     }
 
     public void setKomi(float komi)
     {
         m_komi = komi;
     }
 
     public void setToMove(Color toMove)
     {
         m_toMove = toMove;
     }
 
     /** Set a stone on the board.
         Will remove dead stones.
         Requires: getMoveNumber() == 0
     */
     public void setup(Move m)
     {
         assert(getMoveNumber() == 0);
         play(m);
         if (m.getColor() == Color.BLACK)
             m_setupStonesBlack.add(m.getPoint());
         else if (m.getColor() == Color.WHITE)
             m_setupStonesWhite.add(m.getPoint());
         else
             assert(false);
         ++m_setupNumber;
     }
 
     public void setRules(int rules)
     {
         assert(rules == RULES_JAPANESE || rules == RULES_CHINESE);
         m_rules = rules;
     }
 
     public void undo()
     {
         if (getMoveNumber() == 0)
             return;
         --m_moveNumber;
         MoveRecord r = (MoveRecord)m_moves.get(m_moveNumber);
         Move m = r.getMove();
         Color c = m.getColor();
         Color otherColor = c.otherColor();
         Point p = m.getPoint();
         if (p != null)
         {
             Vector suicide = r.getSuicide();
             for (int i = 0; i < suicide.size(); ++i)
             {
                 Point stone = (Point)suicide.get(i);
                 setColor(stone, c);
             }
             setColor(p, r.getOldColor());
             Vector killed = r.getKilled();
             for (int i = 0; i < killed.size(); ++i)
             {
                 Point stone = (Point)killed.get(i);
                 setColor(stone, otherColor);
             }
             if (c == Color.BLACK)
             {
                 m_capturedB -= suicide.size();
                 m_capturedW -= killed.size();
             }
             else
             {
                 m_capturedW -= suicide.size();
                 m_capturedB -= killed.size();
             }
         }
         m_toMove = r.getOldToMove();
     }
     
     private boolean m_mark[][];
 
     private boolean m_dead[][];
 
     private int m_size;
 
     private int m_capturedB;
 
     private int m_capturedW;
 
     private int m_handicapLine1;
 
     private int m_handicapLine2;
 
     private int m_handicapLine3;
 
     private int m_moveNumber;
 
     private int m_rules = RULES_CHINESE;
 
     private int m_setupNumber;
 
     private float m_komi = 5.5f;
 
     private Vector m_moves = new Vector(361, 361);
 
     private Vector m_setupStonesBlack = new Vector();
 
     private Vector m_setupStonesWhite = new Vector();
 
     private Color m_color[][];
 
     private Color m_score[][];
 
     private Color m_toMove;
 
     private Point m_allPoints[];
 
     private Point m_point[][];
 
     private void checkKill(Point p, Color color, Vector killed)
     {
         assert(isMarkCleared());
         Vector stones = new Vector();
         if (isDead(p, color, stones))
         {
             killed.addAll(stones);
             for (int i = 0; i < stones.size(); ++i)
                 setColor((Point)stones.get(i), Color.EMPTY);
         }
         setMark(stones, false);
         assert(isMarkCleared());
     }
 
     private void clearMark()
     {
         for (int i = 0; i < m_allPoints.length; ++i)
         {
             Point p = m_allPoints[i];
             setMark(p, false);
         }
     }
 
     private void findStones(Point p, Color color, Vector stones)
     {
         Color c = getColor(p);
         if (c != color)
             return;
         if (getMark(p))
             return;
         setMark(p, true);
         stones.add(p);
         Vector adj = getAdjacentPoints(p);
         for (int i = 0; i < adj.size(); ++i)
             findStones((Point)(adj.get(i)), color, stones);
     }
 
     private boolean getMark(Point p)
     {
         return m_mark[p.getX()][p.getY()];
     }
 
     private void initAllPoints()
     {
         m_allPoints = new Point[m_size * m_size];
         int i = 0;
         for (int x = 0; x < m_size; ++x)
             for (int y = 0; y < m_size; ++y)
             {
                 Point p = new Point(x, y);
                 m_allPoints[i++] = p;
                 m_point[x][y] = p;
             }
     }
 
     private boolean isDead(Point p, Color color, Vector stones)
     {
         Color c = getColor(p);
         if (c == Color.EMPTY)
             return false;
         if (c != color)
             return true;
         if (getMark(p))
             return true;
         setMark(p, true);
         stones.add(p);
         Vector adj = getAdjacentPoints(p);
         for (int i = 0; i < adj.size(); ++i)
             if (! isDead((Point)(adj.get(i)), color, stones))
                 return false;
         return true;
     }
 
     private boolean isMarkCleared()
     {
         for (int i = 0; i < m_allPoints.length; ++i)
             if (getMark(m_allPoints[i]))
                  return false;
         return true;
     }    
 
     private boolean isOnHandicapLine(int i)
     {
         return (i == m_handicapLine1
                 || i == m_handicapLine2
                 || i == m_handicapLine3);
     }
 
     private boolean isTerritory(Point p, Vector territory, Color color)
     {
         Color c = getColor(p);
         if (c == color.otherColor() && ! getDead(p))
             return false;
         if (c == color)
         {
             if (getDead(p))
                 return false;
             else
                 return true;
         }
         if (getMark(p))
             return true;
         setMark(p, true);
         territory.add(p);
         Vector adj = getAdjacentPoints(p);
         for (int i = 0; i < adj.size(); ++i)
             if (! isTerritory((Point)(adj.get(i)), territory, color))
                 return false;
         return true;
     }
 
     private void setColor(Point p, Color color)
     {
         m_color[p.getX()][p.getY()] = color;
     }
 
     private void setMark(Point p, boolean value)
     {
         m_mark[p.getX()][p.getY()] = value;
     }
 
     private void setMark(Vector points, boolean value)
     {
         int size = points.size();
         for (int i = 0; i < size; ++i)
             setMark((Point)points.get(i), value);
     }
 
     private void setScore(Point p, Color c)
     {
         m_score[p.getX()][p.getY()] = c;
     }
 }
 
 //-----------------------------------------------------------------------------
