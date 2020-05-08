 package org.libreSubsApplet;
 
 import java.awt.Color;
 import java.awt.ComponentOrientation;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 
 import javax.swing.JLabel;
 
 import org.installer.Installer;
 import org.installer.InstallerFactory;
 
 @SuppressWarnings("serial")
 public class InstallLabel extends JLabel{
 	
 	public InstallLabel() {
 		super("Clique aqui para instalar o libresubs no seu explorador de arquivos");
 		setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 9));
 		setOpaque(true);
 		setBackground(Color.WHITE);
 		setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 		addMouseListener(
 			new MouseAdapter() {
 				@Override
 				public void mouseClicked(final MouseEvent eve) {
 					try {
 						final Installer installer = InstallerFactory.getInstaller();
 						installer.install();
 						setText("Instalado com sucesso");
 					} catch (final IOException e) {
 						setText("Erro ao instalar");
 					}
 				}
 			}
 		);
 	}
 
 }
