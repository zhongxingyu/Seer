 package poo.sca.ui;
 
import java.util.Scanner;

 import poo.sca.SCAFacade;
 
 public class SCA {
 	
 	private SCAFacade facade;
 	
	public void SCA(){
 		this.facade = new SCAFacade();
 	}
 	
 	public void exibirMenu(){
 		StringBuffer menu = new StringBuffer();
 		menu.append(">>> SISTEMA DE CONTROLE ACADMICO <<<<\n");
 		menu.append("    0 - SAIR\n");
 		menu.append("    1 - Cadastrar Disciplina\n");
 		menu.append("Digite a opcao:");
 		boolean fim = false;
 		do{
 			switch(Util.lerInteiro(menu.toString())){
 			case 0:
 				Util.alert("At a prxima!");
 				fim = true;
 				break;
 			case 1:
 				cadastrarDisciplina();
 				break;
 			default:
 				Util.alert("Opo invlida!");
 			}
 		}while(!fim);
 	}
 	
 	
 
 	private void cadastrarDisciplina() {
 		String nome = Util.lerString("Digite o nome da disciplina:");
 		int codigo = Util.lerInteiro("Digite o cdigo da disciplina:");
		facade.criarDisciplina(nome,codigo);
 		
 	}
 
 	public static void main(String[] args) {
 		SCA sca = new SCA();
 		sca.exibirMenu();
 	}
 
 }
