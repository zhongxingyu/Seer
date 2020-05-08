 package screens;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.ControlBuilder;
 import de.lessvoid.nifty.builder.ControlDefinitionBuilder;
 import de.lessvoid.nifty.builder.EffectBuilder;
 import de.lessvoid.nifty.builder.ImageBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.checkbox.builder.CheckboxBuilder;
 import de.lessvoid.nifty.controls.dropdown.builder.DropDownBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 import de.lessvoid.nifty.controls.listbox.builder.ListBoxBuilder;
 import de.lessvoid.nifty.controls.radiobutton.builder.RadioButtonBuilder;
 import de.lessvoid.nifty.controls.radiobutton.builder.RadioGroupBuilder;
 import niftyclass.CommonBuilders;
 import niftyclass.DialogPanelControlDefinition;
 
 /**
  *
  * @author calvin
  */
 public class GameCustomizeControlDefinition {
   public static final String NAME = "listBoxDialogControl";
   private static CommonBuilders builders = new CommonBuilders();
 
   public static void register(final Nifty nifty) {
     new ControlDefinitionBuilder(NAME) {{
       controller(new GameCustomizeController());
       control(new ControlBuilder(DialogPanelControlDefinition.NAME) {{
 
         // the actual list box panel at the top          
         panel(new PanelBuilder() {{
           childLayoutHorizontal();
           control(builders.createLabel("Select Map"));
         }});
         panel(builders.vspacer());
         panel(builders.vspacer());
         panel(new PanelBuilder() {{
           width("100%");
           height("1px");
           backgroundColor("#0008");
         }});
         panel(builders.vspacer());
         
         
         panel(new PanelBuilder() {{
           childLayoutHorizontal();
           control(new RadioGroupBuilder("RadioGroup-2"));
           panel(new PanelBuilder() {{
             childLayoutVertical();
             image(new ImageBuilder("map0") {{
                   filename("Interface/map0.png");
                 }});
            panel(builders.vspacer());
             control(new RadioButtonBuilder("option-0"){{
                 group("RadioGroup-2");
                alignCenter();
             }});
           }});          
           
           panel(builders.hspacer("10px"));
           
           panel(new PanelBuilder() {{
             childLayoutVertical();
             image(new ImageBuilder("map1") {{
                   filename("Interface/map1.png");
                 }});
            panel(builders.vspacer());
             control(new RadioButtonBuilder("option-1"){{
                 group("RadioGroup-2");
                alignCenter();
             }});
           }});          
           
           
           panel(builders.hspacer("10px"));
           
           panel(new PanelBuilder() {{
             childLayoutVertical();
             image(new ImageBuilder("map2") {{
                   filename("Interface/map2.png");
                 }});
            panel(builders.vspacer());
             control(new RadioButtonBuilder("option-2"){{
                 group("RadioGroup-2");
                alignCenter();
             }});
           }});          
                     
           panel(builders.hspacer("10px"));
           
           panel(new PanelBuilder() {{
             childLayoutVertical();
             image(new ImageBuilder("map3") {{
                   filename("Interface/map3.png");
                 }});
            panel(builders.vspacer());
             control(new RadioButtonBuilder("option-3"){{
                 group("RadioGroup-2");
                alignCenter();
             }});
           }});          
           
           
           panel(builders.hspacer("10px"));
           
           panel(new PanelBuilder() {{
             childLayoutVertical();
             image(new ImageBuilder("map4") {{
                   filename("Interface/map4.png");
                 }});
            panel(builders.vspacer());
             control(new RadioButtonBuilder("option-4"){{
                 group("RadioGroup-2");
                alignCenter();
             }});
           }});                
         }});
         
         
         
       }});
     }}.registerControlDefintion(nifty);
   }
 }
