 package gau.models;
 
 import java.util.ArrayList;
 
 /**
  * @author mateusgm
  *
  */
 public class Game {
 
    /**
     * 
     */
    private int[] points;
 
    /**
     * 
     */
    private String name;
 
    /**
     * 
     */
    private ArrayList<RealTeam> podium, teams;
 
    /**
     * 
     */
    PlugBoard plugs;
 
    /**
     * 
     */
    boolean[] podiumCheck;
 
    /**
     * 
     */
    int place;
    
    /**
     * 
     */
    boolean test;
 
    /**
     * @param teamsp
     */
    public Game(final ArrayList<RealTeam> teamsp) {
       teams  = teamsp;
       podium = new ArrayList<RealTeam>();
    }
 
    /**
     * @param namep
     * @param pointsp
     * @param teamsp
     */
    public Game(final String namep, final int[] pointsp,
          final ArrayList<RealTeam> teamsp) {
       name   = namep;
       points = pointsp;
       teams  = teamsp;
       podium = new ArrayList<RealTeam>();
    }
 
    /**
     * @param pointsp
     * @param nteams
     * @return
     */
    public final int[] parsePoints(final String pointsp, final int nteams) {
 
       int[] pointsAux = new int[nteams];
       String[] aux = pointsp.split("/");
       boolean parser = true;
       int end = 0;
 
       for (int i = 0; i < nteams; i++) {
          if (parser) {
             if (i == aux.length) {
                parser = false;
                end = 0;
             } else if (aux[i].equals("...")) {
                parser = false;
                end = pointsAux[i - 1];
             } else {
                end = Integer.parseInt(aux[i]);
             }
          }
          pointsAux[i] = end;
       }
       return pointsAux;
 
    }
 
    /**
     * @return
     */
    public final String results() {
       String aux = "-----------\n";
       for (int i = 0; i < podium.size(); i++) {
          aux += (i + 1) + ". " + podium.get(i);
          if (points != null) {
             aux += " : " + points[i] + "ptos";
          }
          aux += "\n";
       }
       return aux;
    }
 
    /**
     * @return
     */
    public final int size() {
       return teams.size();
    }
 
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
       return name;
    }
 
    /**
     * @return
     * @throws Exception
     */
    public final boolean start(boolean testp) throws Exception {
       test = testp;
       podiumCheck = new boolean[teams.size()];
       place = 1;
       if (!test) {
          plugs = new PlugBoard();
       }
       return true;
    }
 
    /**
     * @return
     * @throws Exception
     */
    public final String resume() throws Exception {
       if (test) {
          return resumeTest();
       } else {
          return resumeReal();
       }
    }
    
    public final String resumeReal() throws Exception {
       boolean changed = false;
       String update = "";
      while (!changed && podium.size() < teams.size()) {
          boolean[] active = plugs.read();
          int i = 0;
          while (i < active.length) {
             if (active[i] && !podiumCheck[i]) {
                podiumCheck[i] = true;
                podium.add(teams.get(i));
                update += (place++) + ". " +  teams.get(i) + "\n";
                changed = true;
                }
             i++;
          }
       }
       return update;
    }
    
    /**
     * @return
     */
    public final String resumeTest() {
       try {
          Thread.sleep(2000);
       } catch (Exception e) {
 
       } finally {
          podiumCheck[place - 1] = true;
          podium.add(teams.get(place - 1));
       }
       return place  + ". " +  teams.get((place++) - 1) + "\n";
    }
 
     /**
     * @return
     */
    public final boolean isActive() {
       return podium.size() < teams.size();
    }
 
    /**
     * @return
     */
    public final String finish() {
       int i = 0;
       String update = "";
       while (i < podiumCheck.length) {
          if (!podiumCheck[i]) {
             podium.add(teams.get(i));
             update += (place++) + ". " +  teams.get(i) + "\n";
          }
          i++;
       }
       return update;
    }
 
 }
