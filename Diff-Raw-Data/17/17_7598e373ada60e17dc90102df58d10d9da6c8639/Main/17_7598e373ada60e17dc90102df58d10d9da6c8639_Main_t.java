 import java.io.*;
 public class Main{
 	public static void main(String args[]){
 		if (args.length > 0){
 			// Flemme de rajouter un constructeur sans parametre
 			PlayListModel playlist = new PlayListModel(args[0]);
 			for (int i = 1; i < args.length; i++)
 			{
				playlist.add(args[i]);
 			}
 			playlist.play();
 		}else{
 			System.out.println("Utilisation : précisez un nom de fichier à lire !!!");
 	}}
 }
