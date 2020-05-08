 package gui;
 
 import fap_java.NPC;
 import fap_java.Params;
 
 import fap_java.Tools;
 
 import java.awt.Graphics;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 
 import gui.versusSelection.characterSelection.*;
 
 public class PlayerSelection extends FAPanel {
     private JButton btnNext = new JButton();
     private JButton btnAdd = new JButton();
     
     private ArrayList<PlayerSelect> players;
     private ArrayList<JComboBox> controlSelecters;
     private ArrayList<JComboBox> teamSelecters;
     private ArrayList<JButton> eraseSelecters;
     private String[] listControls = {"Player 1","Player 2","Player 3","AI Low", "AI Middle", "AI High"};
     private int maxPlayers = Params.maxPlayers;
     
     public PlayerSelection(TheFrame theFrame, JPanel jPanel) {
         super(theFrame, jPanel);
         
         players = new ArrayList<PlayerSelect>();
         controlSelecters = new ArrayList<JComboBox>(); 
         teamSelecters = new ArrayList<JComboBox>(); 
         eraseSelecters = new ArrayList<JButton>();
         // testing
 
         //
         this.addPlayerSelecter();
         this.addPlayerSelecter();
         //---------
         
         swordX = minxS;
         cloudsX = minxC;
         
         this.setLayout(null);
         this.setSize(Constants.frameDimension);
 
         btnGoBack.setText("Retour");
         btnGoBack.setSize(120, 40);
         btnGoBack.setLocation(20, 20);
         
         btnNext.setText("Suivant");
         btnNext.setSize(120, 40);
         btnNext.setLocation(this.getWidth()-30-btnNext.getWidth(), 20);
         
         btnAdd.setText("+");
         btnAdd.setSize(40,30);
         btnAdd.setLocation(140,140);
         btnAdd.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 addPlayerSelecter();
             }
         });
         
         btnNext.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 nextFrame();
             }
         });
         
         this.repaint();
     }
     
     public void nextFrame(){
         
         ArrayList<Integer> listTeams = new ArrayList<Integer>();
         for(int i=0;i<Params.maxPlayers;i++){
             listTeams.add(null);
         }
         // Parse the PlayerSelecters
         for(int i=0;i<players.size();i++){
             PlayerSelect ps = players.get(i);
             if(ps.getControler() > 2){
                 int isFSM = ps.getControler()-1;    // So they get lvl 2,3 and 4
                 ps.setIsFSM(isFSM);
             }
             else{
                 ps.setIsFSM(0);
             }
             // Security for number of teams :
             int team = ps.getTeam();
             if(listTeams.contains(team)){}
             else{
                 listTeams.set(team, team);
             }
         }
         // Security for number of teams :
         listTeams = Tools.removeNull(listTeams);
         for(int i=0;i<players.size();i++){
             PlayerSelect ps = players.get(i);
             ps.setTeam(listTeams.indexOf(ps.getTeam()));
         }
         
         // Proceeding to next panel
         JPanel nextPanel = new CharacterSelection(parent,this);
         parent.changePanel(nextPanel);
     }
     
     public void paintComponent(Graphics g){
         super.paintComponent(g);
     }
 
     public void setPlayers(ArrayList<PlayerSelect> players) {
         this.players = players;
     }
 
     public ArrayList<PlayerSelect> getPlayers() {
         return players;
     }
     
     public void organizePlayerSelect(){
         // Reinit buttons and lists
         this.removeAll();
         this.add(btnNext);
         this.add(btnGoBack);
         this.add(btnAdd);
         this.validate();
         this.repaint();
         
         for(int i=0;i<this.players.size();i++){
             // PlayerSelect
             PlayerSelect ps = players.get(i);
             // Associated controller
             JComboBox combo = this.controlSelecters.get(i);
             combo.setSelectedIndex(ps.getControler());
             combo.setBounds(200, 200+50*i, 150, 30);
             //this.remove(combo);
             this.add(combo);
             combo.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     combo_ActionPerformed(e);
                 }
             });
             // Associated Team
             JComboBox team = this.teamSelecters.get(i);
             team.setSelectedIndex(ps.getTeam());
             team.setBounds(370, 200+50*i, 150, 30);
             //this.remove(team);
             this.add(team);
             team.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     team_ActionPerformed(e);
                 }
             });
             // Delete button
             JButton jb = this.eraseSelecters.get(i);
             jb.setText("X");
             jb.setBounds(140, 200+50*i, 40, 30);
             this.add(jb);
             jb.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     jb_ActionPerformed(e);
                 }
             });
         }
     }
     
     public void jb_ActionPerformed(ActionEvent e){
         JButton jb = (JButton) e.getSource();
         this.removePlayerSelecter(this.eraseSelecters.indexOf(jb));
     }
     
     public void combo_ActionPerformed(ActionEvent e){
         JComboBox combo = (JComboBox)e.getSource();
         int controler = combo.getSelectedIndex();
         PlayerSelect ps = players.get(this.controlSelecters.indexOf(combo));
         ps.setControler(controler);
     }
     
     public void team_ActionPerformed(ActionEvent e){
         JComboBox combo = (JComboBox)e.getSource();
         int team = combo.getSelectedIndex();
         PlayerSelect ps = players.get(this.teamSelecters.indexOf(combo));
         ps.setTeam(team);
     }
     
     public void addPlayerSelecter(){
         if(players.size()<this.maxPlayers){
             players.add(new PlayerSelect(this));
             controlSelecters.add(new JComboBox(listControls));
             JComboBox teamS = new JComboBox();
             for(int i=0;i<maxPlayers;i++){
                 teamS.addItem("Team "+(i+1));
             }
             teamSelecters.add(teamS);
             eraseSelecters.add(new JButton());
             
             this.organizePlayerSelect();
         }
     }
     
     public void removePlayerSelecter(int id){
        if(players.size()>2){
            players.remove(id);
            controlSelecters.remove(id);
            teamSelecters.remove(id);
            eraseSelecters.remove(id);
            
            this.organizePlayerSelect();
         }
     }
 
     public void setMaxPlayers(int maxPlayers) {
         this.maxPlayers = maxPlayers;
     }
 
     public int getMaxPlayers() {
         return maxPlayers;
     }
     
 }
