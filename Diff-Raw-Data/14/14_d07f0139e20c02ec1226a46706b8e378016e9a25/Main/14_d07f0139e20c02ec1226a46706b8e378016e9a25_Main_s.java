 package gestaogui;
 
 import gestaologica.*;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 /**
  * Main class of the program.
  *
  */
 public class Main {
 
     private static JanelaPrincipal maingui;
     public static ArrayList<SalaAula> salas;
     public static ArrayList<Aluno> alunos;
     public static ArrayList<Professor> professores;
     public static ArrayList<Turma> turmas;
     public static ArrayList<Disciplina> disciplinas;
     public static ArrayList<Horario> horarios;
     public static CSV ola = new CSV();
 
     public static void main(String[] args) throws IOException, ClassNotFoundException {
         //Inserção dos ficheiros de alunos, professores, disciplinas, salas, turmas, e horários.
         lerEstado();
 
         Imagens img = new Imagens();
        //if (ola.isDfgh() == true || ola.isGhjk() == true) {
        @SuppressWarnings("unused")
        SplashScreen splash = new SplashScreen(3000, img.splash, true);
 
        try {
            maingui = new JanelaPrincipal();
        } catch (URISyntaxException e) {
         }
     }
 
     private static void lerEstado() throws IOException, ClassNotFoundException {
         ola.lerEstado();
         if (!ola.getAluno().isEmpty() && !ola.getDisciplina().isEmpty() && !ola.getHorario().isEmpty() && !ola.getProfessor().isEmpty() && !ola.getSalaAula().isEmpty() && !ola.getTurma().isEmpty()) {
             salas = ola.getSalaAula();
             alunos = ola.getAluno();
             professores = ola.getProfessor();
             disciplinas = ola.getDisciplina();
             turmas = ola.getTurma();
             horarios = ola.getHorario();
             System.out.println("Ficheiros guardados da utilização anterior.");
         } else {
             salas = ola.Sala();
             alunos = ola.Alunos();
             professores = ola.Professor();
             disciplinas = ola.Disciplinas();
             turmas = ola.Turmas();
             horarios = ola.Horario();
             System.out.println("Novo.");
         }
 
     }
 
     public static void gravarEstado() {
         ola.gravarEstado();
     }
 }
