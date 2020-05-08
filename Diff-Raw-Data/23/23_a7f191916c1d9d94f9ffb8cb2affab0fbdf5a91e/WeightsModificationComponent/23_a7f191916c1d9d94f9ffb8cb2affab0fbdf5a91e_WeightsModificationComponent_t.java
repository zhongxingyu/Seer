 /**
  * @author Hadrien Milano <Sciper : 224340>
  * @author Christophe Tafani-Dereeper <Sciper : 223529>
  */
 
 package ch.epfl.flamemaker.gui;
 
 import java.awt.event.FocusAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFormattedTextField.AbstractFormatter;
 import javax.swing.JLabel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingUtilities;
 
 import ch.epfl.flamemaker.flame.ObservableFlameBuilder;
 import ch.epfl.flamemaker.flame.Variation;
 
 @SuppressWarnings("serial")
 public class WeightsModificationComponent extends JComponent {
 
 	final private ObservableFlameBuilder flameBuilder;
 	private int selectedTransformationIndex;
 	final private ArrayList<JFormattedTextField> fields = new ArrayList<JFormattedTextField>();
 
 	public WeightsModificationComponent(
 			final ObservableFlameBuilder flameBuilder) {
 
 		this.flameBuilder = flameBuilder;
 
 		GroupLayout weightsGroup = new GroupLayout(this);
 		this.setLayout(weightsGroup);
 
 		SequentialGroup H = weightsGroup.createSequentialGroup();
 		weightsGroup.setHorizontalGroup(H);
 		SequentialGroup V = weightsGroup.createSequentialGroup();
 		weightsGroup.setVerticalGroup(V);
 
 		// Autant de groupes verticaux que de variations, 2 lignes
 		ArrayList<ParallelGroup> verticalGroups = new ArrayList<ParallelGroup>();
 		ParallelGroup currentGroup;
 		for (int i = 0; i < Variation.ALL_VARIATIONS.size(); i++) {
 			currentGroup = weightsGroup.createParallelGroup();
 			verticalGroups.add(currentGroup);
 			H.addGroup(currentGroup);
 		}
 		ArrayList<ParallelGroup> horizontalGroups = new ArrayList<ParallelGroup>();
 		for (int i = 0; i < 2; i++) {
 			currentGroup = weightsGroup.createParallelGroup();
 			horizontalGroups.add(currentGroup);
 			V.addGroup(currentGroup);
 		}
 		int h = 0, v = 0;
 		for (Variation variation : Variation.ALL_VARIATIONS) {
 			final JLabel label = new JLabel(variation.name());
 			final JFormattedTextField formattedTextField = buildFormattedTextField();
 			formattedTextField.setInputVerifier(new WeightInputVerifier());
 			fields.add(formattedTextField);
			
 			formattedTextField.addPropertyChangeListener("value",
 					new PropertyChangeListener() {
 
 				@Override
 				public void propertyChange(PropertyChangeEvent evt) {
 					double newWeight = ((Number) formattedTextField
 							.getValue()).doubleValue();
 					Variation concernedVariation = null;
 					for (Variation v : Variation.ALL_VARIATIONS) {
 						if (v.name().equals(label.getText())) {
 							concernedVariation = v;
 						}
 					}
 
					if (selectedTransformationIndex != -1 
							&& concernedVariation != null 
							&& flameBuilder.getTransformation(selectedTransformationIndex).weight(concernedVariation) != newWeight ) {
 						flameBuilder.setVariationWeight(
 								selectedTransformationIndex,
 								concernedVariation, newWeight);
 					}
 				}
 			});
 
 			horizontalGroups.get(h).addComponent(label);
 			verticalGroups.get(v).addComponent(label);
 
 			horizontalGroups.get(h).addComponent(formattedTextField);
 			verticalGroups.get(v + 1).addComponent(formattedTextField);
 
 			v += 2;
 			// Si y'a eu 6 éléments ajoutés => v = 4
 			if (v % 6 == 0) {
 				h++;
 				v = 0;
 			}
 
 			H.addPreferredGap(ComponentPlacement.UNRELATED);
 		}
 
 	}
 
 	public void setSelectedTransformationIndex(int id) {
 		if (id != -1) {
 			this.selectedTransformationIndex = id;
 
 			/*
 			 * On récupère le tableau des poids de la nouvelle transformation,
 			 * et on remplit nos 6 champs avec
 			 */
 			double[] newWeights = flameBuilder.getTransformation(id).weights();
 			int i = 0;
 			for (double weight : newWeights) {
 				fields.get(i).setValue(weight);
 				i++;
 			}
 		}
 	}
 
 	/* TODO : faire une classe utils ? (méthode dupliquée) */
 	private JFormattedTextField buildFormattedTextField() {
 		final JFormattedTextField field = new JFormattedTextField(
 				new DecimalFormat("#0.##"));
 		field.setValue(0);
 		field.setColumns(3);
 
 		/*
 		 * On fait en sorte que les valeurs des champs se sélectionnent au focus
 		 * Note : On doit emballer le selectAll() dans un invokeLater, sinon le
 		 * formattage du champ enlève la sélection
 		 */
 		field.addFocusListener(new FocusAdapter() {
 			public void focusGained(java.awt.event.FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
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
 	 * Verificateur pour les inputs des poids
 	 */
 	private class WeightInputVerifier extends InputVerifier {
 
 		@Override
 		public boolean verify(JComponent input) {
 			JFormattedTextField tf = (JFormattedTextField) input;
 
 			try {
 				String text = tf.getText();
 
 				AbstractFormatter formatter = tf.getFormatter();
 
 				Number value = (Number) formatter.stringToValue(text);
 
 				/*
 				 * On n'utilise pas setText, mais setValue à la place car
 				 * setText pose des problèmes puisque swing a la riche idée de
 				 * traduire les nombres (remplacer les "." par des ",")... Le
 				 * champ est valide si le poids est compris entre 0 et 1
 				 */
				if ((value.doubleValue() < 0 || value.doubleValue() > 1)) {
 					tf.setValue(tf.getValue());
 				} else {
 					tf.setValue(value);
 				}
 
 			} catch (ParseException e) {
 				/*
 				 * On n'a rien à gérer ici car le texte est automatiquement
 				 * remplacé par la valeur précédente.
 				 */
 			}
 			return true;
 		}
 
 	}
 
 }
