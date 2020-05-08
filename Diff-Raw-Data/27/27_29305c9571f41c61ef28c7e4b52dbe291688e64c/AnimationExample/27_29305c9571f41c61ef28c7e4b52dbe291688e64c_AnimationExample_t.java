 package wingset;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.wings.SButton;
 import org.wings.SComboBox;
 import org.wings.SComponent;
 import org.wings.SDialog;
 import org.wings.SFlowDownLayout;
 import org.wings.SLabel;
 import org.wings.SPanel;
 import org.wings.STextField;
 import org.wings.style.CSSProperty;
 import org.wingx.animation.Animation;
 import org.wingx.animation.ColorAnimation;
 import org.wingx.animation.Motion;
 
 public class AnimationExample extends WingSetPane {
 
 	private AnimationControls controls;
 
 	private SPanel panel;
 
 	@Override
 	protected SComponent createControls() {
 		controls = new AnimationControls();
 		return controls;
 	}
 
 	@Override
 	protected SComponent createExample() {
 		panel = new SPanel(new SFlowDownLayout());
 		
 		final SLabel hello = new SLabel("Hello");
 		
 		final SPanel content = new SPanel();
 		
 		SButton showHide = new SButton("Show");
 		showHide.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
                if (hello.getParent() != null)
                    content.remove(hello);
 				content.add(hello);
 				
 				Animation animation = new Animation(content, 2, 300, 200);
 				animation.start();
 			}
 		});
 		
 		SPanel head = new SPanel();
 		head.add(new SLabel("wingS Information"));
 		head.add(showHide);
 		
 		panel.add(head);
 		panel.add(content);
 		
 		return panel;
 	}
 
 	private class AnimationControls extends ComponentControls {
 
 		public AnimationControls() {
 
 			addControl(new SLabel("From"));
 			final SComboBox fromColors = new SComboBox(createColors());
 			addControl(fromColors);
 			addControl(new SLabel("To"));
 			final SComboBox toColors = new SComboBox(createColors());
 			addControl(toColors);
 			addControl(new SLabel("Duration"));
 			final SComboBox durations = new SComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
 			addControl(durations);
 			addControl(new SLabel("X-Pos"));
 			final STextField xPos = createNumberField();
 			addControl(xPos);
 			addControl(new SLabel("Y-Pos"));
 			final STextField yPos = createNumberField();
 			addControl(yPos);
 
 			final SButton animate = new SButton("Animate");
 			animate.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent e) {
 					Color fromColor = ((ColorWrapper) fromColors.getSelectedItem()).getColor();
 					Color toColor = ((ColorWrapper) toColors.getSelectedItem()).getColor();
 					Integer duration = (Integer) durations.getSelectedItem();
 
 					ColorAnimation animation = new ColorAnimation(panel, duration, fromColor, toColor);
 					animation.start();
 					
 					int x = 0;
 					int y = 0;
 					try {
 						x = Integer.parseInt(xPos.getText());
 						y = Integer.parseInt(yPos.getText());
 					} catch (NumberFormatException e1) {
 						e1.printStackTrace();
 					}
 					
 					Motion motion = new Motion(panel, duration, x, y);
 					motion.start();
 					
 					ColorAnimation buttonAnimation = new ColorAnimation(animate, duration, fromColor, toColor);
 					buttonAnimation.start();
 				}
 			});
 			addControl(animate);
 			
 			final SButton animateDialog = new SButton("Animate Dialog");
 			animateDialog.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent e) {
 					SDialog dialog = new SDialog(getSession().getRootFrame(), "Animated Dialog");
 					dialog.add(new SLabel("Label"));
 					dialog.setVisible(true);
 					
 					Integer duration = (Integer) durations.getSelectedItem();
 					
 					Animation animation = new Animation(dialog, duration, 300, 200);
 					animation.start();
 					
 //					Motion motion = new Motion(dialog, duration, 5, 5);
 //					motion.start();
 				}
 			});
 			addControl(animateDialog);
 		}
 	}
 
 	private static ColorWrapper[] createColors() {
 		return new ColorWrapper[] {
 				new ColorWrapper(Color.WHITE),
 				new ColorWrapper(Color.RED), 
 				new ColorWrapper(Color.ORANGE),
 				new ColorWrapper(Color.GREEN), 
 				new ColorWrapper(Color.BLUE),
 				new ColorWrapper(Color.BLACK),
 				new ColorWrapper(Color.YELLOW),
 				new ColorWrapper(Color.CYAN),
 				new ColorWrapper(Color.DARK_GRAY),
 				new ColorWrapper(Color.GRAY),
 				new ColorWrapper(Color.LIGHT_GRAY),
 				new ColorWrapper(Color.MAGENTA),
 				new ColorWrapper(Color.PINK)
 				};
 	}
 	
 	private static STextField createNumberField() {
         STextField field = new STextField();
         field.setAttribute(CSSProperty.TEXT_ALIGN, "right");
         return field;
     }
 
 	static class ColorWrapper extends SLabel {
 
 		Color color;
 
 		ColorWrapper(Color c) {
 			color = c;
 			if (Color.BLACK.equals(c)) {
 				setForeground(Color.WHITE);
 			}
 			setBackground(c);
 			setText(c.toString());
 		}
 
 		Color getColor() {
 			return color;
 		}
 	}
 }
