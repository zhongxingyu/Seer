 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fases;
 
 import guitarra.Guitarra;
 import java.awt.Component;
 import java.awt.Graphics;
 import javaPlay.GameEngine;
 import javaPlay.GameStateController;
 import javaPlay.Keyboard;
 import javaPlayExtras.Imagem;
 import javaPlayExtras.Keys;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import utilidades.MIDIReader;
 import utilidades.Video;
 
 /**
  *
  * @author fernando_mota
  */
 public abstract class Musica implements GameStateController {
     private int level;
     private float[][] notas;
     private String midiFile;
     private String videoFile;
     private String bgFile;
     private String guitarraFile;
     private Imagem bgImageFundo;
     private JLabel bgImageFundoEsquerda;
     private JLabel progresso;
     private JLabel pontuacao;
     private Imagem bgImageFundoDireita;
     private Guitarra guitarra;
     private boolean musicLoaded = false;
     private boolean videoStarted = false;
     private MIDIReader musica;
     private Video video;
     private JPanel thePanel;
     private Component theVideo;
     private boolean pause = false;
    private int totalTimeElapsed = 0;
     public Musica(){
     }
     protected void setMusica(String musicFile, int level){
         this.midiFile = musicFile;
          this.guitarra = Guitarra.getInstance();
          this.level = level;
     }
     public Musica(String musicFile,String videoFile, String fundo, String guitarra, int level){
          this.midiFile = musicFile;
          this.videoFile = videoFile;
          this.bgFile = fundo;
          this.guitarraFile = guitarra;
          this.guitarra = Guitarra.getInstance();
          this.level = level;
     }
     public Musica(float[][] notas, String videoFile, String fundo, String guitarra, int level){
          this.notas = notas;
          this.videoFile = videoFile;
          this.bgFile = fundo;
          this.guitarraFile = guitarra;
          this.guitarra = Guitarra.getInstance();
          this.level = level;
     }
     public void load() {
         if(this.videoFile != null){
             this.video = new Video(this.videoFile);
         }
     }
 
     public void unload() {
     }
     public abstract void gameOver();
     public abstract void nextMusic();
     public void start() {
         if(this.midiFile != null){
             this.musica = new MIDIReader(this.midiFile);
         }
         else{
             this.musica = null;
         }
          try {
             if(this.bgFile != null && this.guitarraFile != null){
                 this.bgImageFundoEsquerda =  new JLabel(new ImageIcon(this.bgFile));
                 this.bgImageFundoDireita = new Imagem(this.guitarraFile);
             }
             else{
                 this.bgImageFundoEsquerda  =new JLabel(new ImageIcon("img_cenario/fundo.png"));
                 this.bgImageFundoDireita  = new Imagem("img_cenario/guitarra_fundo.png");
             }
             
             
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage());
         }
         
         this.guitarra.setLevel(this.level);
         thePanel = new JPanel();
         thePanel.setLayout(null);
         if(this.video != null){
             theVideo = this.video.getSwingComponent();
         }     
           
         thePanel.add(this.bgImageFundoEsquerda);
         thePanel.setVisible(true);
         if(this.theVideo != null){
             this.thePanel.add(theVideo);
             this.theVideo.setVisible(true);
             this.theVideo.setBounds(0,301,429,319);
             this.theVideo.repaint();
         }    
         this.bgImageFundoEsquerda.setVisible(true);
         this.bgImageFundoEsquerda.setBounds(0,0,432,theVideo!=null?301:620);
         thePanel.setBounds(0,0,429, 620);         
         this.bgImageFundoEsquerda.repaint();
         thePanel.repaint();
         GameEngine.getInstance().getGameCanvas().setPanel(thePanel);
         this.guitarra.setMinorTime();
         if(this.musica != null){
             this.musica.setInterval(0.5f);
             this.musica.refresh();
         }
         this.changeProgressImage();
         this.changePontuation();
     }
      private void changeProgressImage(){
         JLabel novoProgresso = this.guitarra.getImageProgress();
         if(this.progresso == novoProgresso){
             return;
         }
         if(this.progresso != null){
             thePanel.remove(this.progresso);
         }
         novoProgresso.setBounds(10,10,107,107);
         novoProgresso.setVisible(true);
         novoProgresso.setLayout(null);
         this.progresso = novoProgresso;
         this.thePanel.add(novoProgresso, new Integer(1),0);
         this.thePanel.repaint();
         
     }
      private void changePontuation(){
         JLabel novoPontuacao =  this.guitarra.getImagePontuation();
         if(this.pontuacao == null){
               novoPontuacao.setBounds(-30,127,306,127);
               novoPontuacao.setVisible(true);
               novoPontuacao.setLayout(null);
               this.pontuacao = novoPontuacao;
               this.thePanel.add(novoPontuacao, new Integer(2),0); 
         }
         this.thePanel.repaint();
     }
     @Override
     public void step(long timeElapsed) {
         Keyboard teclado = GameEngine.getInstance().getKeyboard();
        totalTimeElapsed += timeElapsed;
        
         if(teclado.keyDown(Keys.P)){
            if(totalTimeElapsed>200){
                totalTimeElapsed = 0;
            }
            else{
                return;
            }
            
             this.pause = !this.pause;
             if(this.pause){
                 this.video.pause();
             }
             else{
                 this.video.play();
             }
         }
         if(this.pause){
             return;
         }
         
        this.changeProgressImage();
        this.changePontuation();
         if(this.musicLoaded==false){
             
             /*int[][] notas = new int[Utilidades.getNumeroRandomico(5, 200)][6];
             for(int c=0;c<notas.length;++c){
                 notas[c][0] = Utilidades.getNumeroRandomico(1, 300);
                 for(int c2 = 1;c2<Utilidades.getNumeroRandomico(1, 5); ++c2){
                     notas[c][c2] = Utilidades.sorteia()?c2:0;
                 }
             }*/
             
             if(this.musica != null){
                 if(this.video != null){
                     this.musica.setDuration(video.getDuration());
                 }
                 this.notas = this.musica.getNotes();
             }
             this.guitarra.reset();
             this.guitarra.setNotas(this.notas);
             this.musicLoaded = true;
         }
         else{
             if(this.guitarra.isFirstNotePlayed() && this.videoStarted == false){
                 if(this.video != null){
                     this.video.play();
                 }
                 this.videoStarted = true;
             }
             else if(this.videoStarted == true){
                 if(this.video != null){
                     timeElapsed = 0;
                     this.guitarra.addVideoTime(this.video.getActualTime()*1000);
                 }
             }
             this.guitarra.step(timeElapsed);
         }
         if(this.guitarra.isGameOver()){
             GameEngine.getInstance().getGameCanvas().setPanel(null);
             this.gameOver();
         }
         else if(this.guitarra.isTerminated()){
             GameEngine.getInstance().getGameCanvas().setPanel(null);
             if(this.guitarra.isWinned()){
                 this.nextMusic();
             }
             else{
                 this.gameOver();
             }
         }
     }
 
     public void draw(Graphics g) {
         // g.fillRect(0, 0, 3000, 2400);
         //this.bgImageFundo.draw(g, 0, 0);
         this.bgImageFundoDireita.draw(g, 0, 0);
         this.guitarra.draw(g);
     }
 
     public void stop() {
         if(this.video!=null){
             this.video.reset();
         }
         this.musicLoaded = false;
         this.videoStarted = false;
     }
     
 }
