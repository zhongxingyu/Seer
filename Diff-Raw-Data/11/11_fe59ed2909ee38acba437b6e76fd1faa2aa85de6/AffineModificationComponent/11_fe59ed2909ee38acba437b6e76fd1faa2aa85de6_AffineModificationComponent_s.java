 package ch.epfl.flamemaker.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFormattedTextField.AbstractFormatter;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 import ch.epfl.flamemaker.concurrent.ObservableFlameBuilder;
 import ch.epfl.flamemaker.geometry2d.AffineTransformation;
 
 @SuppressWarnings("serial")
 public class AffineModificationComponent extends JComponent {
 
 	private int selectedTransformationIndex;
 	
 	public AffineModificationComponent(final ObservableFlameBuilder flameBuilder){
 		
 		GroupLayout affineGroup = new GroupLayout(this);
 		
 		this.setLayout(affineGroup);
 		
 		// Mise en page de l'édition de la partie affine
 		SequentialGroup H = affineGroup.createSequentialGroup();
 		affineGroup.setHorizontalGroup(H);
 		SequentialGroup V = affineGroup.createSequentialGroup();
 		affineGroup.setVerticalGroup(V);
 		
 		ParallelGroup H1 = affineGroup.createParallelGroup();
 		H.addGroup(H1);
 		ParallelGroup H2 = affineGroup.createParallelGroup();
 		H.addGroup(H2);
 		ParallelGroup H3 = affineGroup.createParallelGroup();
 		H.addGroup(H3);
 		ParallelGroup H4 = affineGroup.createParallelGroup();
 		H.addGroup(H4);
 		ParallelGroup H5 = affineGroup.createParallelGroup();
 		H.addGroup(H5);
 		ParallelGroup H6 = affineGroup.createParallelGroup();
 		H.addGroup(H6);
 		
 		ParallelGroup V1 = affineGroup.createParallelGroup();
 		V.addGroup(V1);
 		ParallelGroup V2 = affineGroup.createParallelGroup();
 		V.addGroup(V2);
 		ParallelGroup V3 = affineGroup.createParallelGroup();
 		V.addGroup(V3);
 		ParallelGroup V4 = affineGroup.createParallelGroup();
 		V.addGroup(V4);
 		
 		
 		// Ligne "translation"
 		JLabel translationLabel = new JLabel("Translation");
 		H1.addComponent(translationLabel);
 		V1.addComponent(translationLabel);
 		
 		final JFormattedTextField translationFactor = buildFormattedTextField();
 		H2.addComponent(translationFactor);
 		V1.addComponent(translationFactor);
 		
 		JButton translationLeftButton = new JButton("←");
 		translationLeftButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation.newTranslation(-1*((Number) translationFactor.getValue()).doubleValue(), 0)
 						.composeWith(t)
 				);
 			}	
 		});
 		H3.addComponent(translationLeftButton);
 		V1.addComponent(translationLeftButton);
 		
 		JButton translationRightButton = new JButton("→");
 		translationRightButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation.newTranslation(((Number) translationFactor.getValue()).doubleValue(), 0)
 						.composeWith(t)
 				);
 			}	
 		});
 		H4.addComponent(translationRightButton);
 		V1.addComponent(translationRightButton);
 		
 		JButton translationUpButton = new JButton("↑");
 		translationUpButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation.newTranslation(0, ((Number) translationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 			}	
 		});
 		H5.addComponent(translationUpButton);
 		V1.addComponent(translationUpButton);
 		
 		JButton translationDownButton = new JButton("↓");
 		translationDownButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation.newTranslation(0, -1 * ((Number) translationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 			}	
 		});
 		H6.addComponent(translationDownButton);
 		V1.addComponent(translationDownButton);
 		
 		
 		// Ligne "Rotation"
 		JLabel rotationLabel = new JLabel("Rotation");
 		H1.addComponent(rotationLabel);
 		V2.addComponent(rotationLabel);
 		
 		final JFormattedTextField rotationFactor = buildFormattedTextField();
 		H2.addComponent(rotationFactor);
 		V2.addComponent(rotationFactor);
 		
 		JButton rotationPositiveButton = new JButton("↺");
 		rotationPositiveButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
 						.newRotation((Math.PI/180) * ((Number) rotationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 			}	
 		});
 		H3.addComponent(rotationPositiveButton);
 		V2.addComponent(rotationPositiveButton);
 		
 		JButton rotationNegativeButton = new JButton("↻");
 		rotationNegativeButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
 						.newRotation(-(Math.PI/180) * ((Number) rotationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 			}	
 		});
 		H4.addComponent(rotationNegativeButton);
 		V2.addComponent(rotationNegativeButton);
 		
 		
 		// Ligne "Dilatation"
 		JLabel dilatationLabel = new JLabel("Dilatation");
 		H1.addComponent(dilatationLabel);
 		V3.addComponent(dilatationLabel);
 		
 		final JFormattedTextField dilatationFactor = buildFormattedTextField();
 		dilatationFactor.setInputVerifier(new DilatationInputVerifier());
 		
 		H2.addComponent(dilatationFactor);
 		V3.addComponent(dilatationFactor);
 		
 		final JButton dilatationHPlusButton = new JButton("+ ↔");
 		dilatationHPlusButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
 						.newScaling(((Number) dilatationFactor.getValue()).doubleValue(), 1)
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H3.addComponent(dilatationHPlusButton);
 		V3.addComponent(dilatationHPlusButton);
 		
 		JButton dilatationHMinusButton = new JButton("- ↔");
 		dilatationHMinusButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Translation d'abord
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
 						.newScaling(1/((Number) dilatationFactor.getValue()).doubleValue(), 1)
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H4.addComponent(dilatationHMinusButton);
 		V3.addComponent(dilatationHMinusButton);
 		
 		JButton dilatationVPlusButton = new JButton("+ ↕");
 		dilatationVPlusButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
 						.newScaling(1, ((Number) dilatationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H5.addComponent(dilatationVPlusButton);
 		V3.addComponent(dilatationVPlusButton);
 		
 		JButton dilatationVMinusButton = new JButton("- ↕");
 		dilatationVMinusButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
 						.newScaling(1, 1/((Number) dilatationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H6.addComponent(dilatationVMinusButton);
 		V3.addComponent(dilatationVMinusButton);
 		
 		
 		// Ligne "Transvection"
 		JLabel transvectionLabel = new JLabel("Transvection");
 		H1.addComponent(transvectionLabel);
 		V4.addComponent(transvectionLabel);
 		
 		final JFormattedTextField transvectionFactor = buildFormattedTextField();
 		H2.addComponent(transvectionFactor);
 		V4.addComponent(transvectionFactor);
 		
 		JButton transvectionLeftButton = new JButton("←");
 		transvectionLeftButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println(transvectionFactor.getValue());
 				System.out.println(transvectionFactor.getValue().getClass());
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
						.newShearX(((Number)dilatationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H3.addComponent(transvectionLeftButton);
 		V4.addComponent(transvectionLeftButton);
 		
 		JButton transvectionRightButton = new JButton("→");
 		transvectionRightButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
						.newShearX(-((Number) dilatationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H4.addComponent(transvectionRightButton);
 		V4.addComponent(transvectionRightButton);
 		
 		JButton transvectionUpButton = new JButton("↑");
 		transvectionUpButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
						.newShearY(((Number) dilatationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H5.addComponent(transvectionUpButton);
 		V4.addComponent(transvectionUpButton);
 		
 		JButton transvectionDownButton = new JButton("↓");
 		transvectionDownButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AffineTransformation t = flameBuilder.affineTransformation(selectedTransformationIndex);
 				flameBuilder.setAffineTransformation(selectedTransformationIndex, 
 					AffineTransformation
						.newShearY(-((Number) dilatationFactor.getValue()).doubleValue())
 						.composeWith(t)
 				);
 				
 			}	
 		});
 		H6.addComponent(transvectionDownButton);
 		V4.addComponent(transvectionDownButton);
 		
 
 		// ← → ↑ ↓ ⟲ ⟳ ↔ ↕
 	}
 	
 	/**
 	 * Construit un textField servant à entrer le poids de la modification
 	 * ( Evite la duplication de code sur cette tache répétitive)
 	 * @return
 	 */
 	/* TODO : faire une classe utils ? (méthode dupliquée) */
 	private JFormattedTextField buildFormattedTextField(){
 		final JFormattedTextField field = new JFormattedTextField(new DecimalFormat("#0.##"));
 		field.setValue(1);
 		field.setColumns(3);
 		
 		/* 
 		 * On fait en sorte que les valeurs des champs se sélectionnent au focus 
 		 * Note : On doit emballer le selectAll() dans un invokeLater, sinon le formattage 
 		 * du champ enlève la sélection
 		 */
 		field.addFocusListener(new FocusAdapter() {
 		    public void focusGained(java.awt.event.FocusEvent evt) {
 	   	    	SwingUtilities.invokeLater( new Runnable() {
     				@Override
     				public void run() {
     					field.selectAll();		
     				}
     			});
 		    }
 		});
 		
 		return field;
 	}
 	
 	/**
 	 * Verificateur pour l'input de dilatation
 	 */
 	private class DilatationInputVerifier extends InputVerifier{
 
 		@Override
 		public boolean verify(JComponent input){
 			JFormattedTextField tf = (JFormattedTextField) input;
 			
 			try {
 				String text = tf.getText();
 				
 				AbstractFormatter formatter = tf.getFormatter();
 				
 				// Récupère la valeur dans un double dans tous les cas
 				
 				Number value = (Number) formatter.stringToValue(text);
 				
 				/* 
 				 * On n'utilise pas setText, mais setValue à la place car setText pose des problèmes
 				 * puisque swing a la riche idée de traduire les nombres (remplacer les "." par des ",")...
 				 */
 				if(value.doubleValue() == 0){
 					tf.setValue(tf.getValue());
 				} else {
 					tf.setValue(value);
 				}
 				
 			} catch (ParseException e) {
 				/* 
 				 * On n'a rien à gérer ici car le texte est automatiquement remplacé par 
 				 * la valeur précédente.
 				 */
 			}
 			return true;
 		}
 		
 	}
 
 	public void setSelectedTransformationIndex(int id) {
 		this.selectedTransformationIndex = id;		
 	}
 }
