 package com.vaadin.toolkitdraw.tools;
 
 import java.awt.Color;
 
 import com.vaadin.colorpicker.ColorSelector;
 import com.vaadin.colorpicker.ColorSelector.ColorChangeListener;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.toolkitdraw.components.TwinColorPicker;
 import com.vaadin.toolkitdraw.components.paintcanvas.PaintCanvas;
 import com.vaadin.toolkitdraw.components.paintcanvas.PaintCanvas.Interactive;
 import com.vaadin.toolkitdraw.components.paintcanvas.enums.BrushType;
 import com.vaadin.toolkitdraw.util.IconFactory;
 import com.vaadin.toolkitdraw.util.IconFactory.Icons;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.Slider;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Slider.ValueOutOfBoundsException;
 
 public class Square extends Tool implements ValueChangeListener, ColorChangeListener, ClickListener{
 	
 	private static final long serialVersionUID = 1L;
 
 	private Layout layout = new VerticalLayout();
 	
 	private Slider size;
 	
 	private Slider opacity;
 
 	private TwinColorPicker colorpicker;
 	
 	private CheckBox disableFillcolor;
 	
 	public Square(PaintCanvas canvas){
 			
 			this.canvas = canvas;
 			this.layout.setMargin(true);
 			
 			button = new Button();
 			button.setStyleName(Button.STYLE_LINK);
 			button.setData(BrushType.SQUARE);
 			button.setIcon(IconFactory.getIcon(Icons.ICON_SQUARE));
 			
 			//Create the basic tools
 			GridLayout basic = new GridLayout(2,2);
 			basic.setColumnExpandRatio(0, 1);
 			basic.setColumnExpandRatio(1, 5);
 			basic.setWidth("100%");
 			basic.setHeight("70px");
 					
 			colorpicker = new TwinColorPicker();
 			colorpicker.addListener(this);
 			basic.addComponent(colorpicker,0,0,0,1);
 			basic.setComponentAlignment(colorpicker, Alignment.MIDDLE_LEFT);
 			
 			size = new Slider("Size",1,10);
 			size.addListener(this);
 			size.setSizeFull();
 			size.setImmediate(true);		
 			try{
 				size.setValue(1);
 			}catch(ValueOutOfBoundsException voobe){
 				//NOP
 			}		
 			basic.addComponent(size, 1,0);
 			
 			opacity = new Slider("Opacity",0,100);
 			opacity.addListener(this);
 			opacity.setSizeFull();
 			opacity.setImmediate(true);
 			try {
 				opacity.setValue(0);
 			} catch (ValueOutOfBoundsException voobe) {
 				//NOP
 			}
 			basic.addComponent(opacity,1,1);
 					
 			layout.addComponent(basic);			
 			
 			//More options
 			disableFillcolor = new CheckBox("Filled rectangle",this);
 			disableFillcolor.setImmediate(true);
 			disableFillcolor.setValue(false);
 			
 			layout.addComponent(disableFillcolor);
 		}
 		
 		public Layout createToolOptions(){	
 			return layout;
 		}	
 			
 		public BrushType getType(){
 			return BrushType.SQUARE;
 		}
 	
 		public void valueChange(ValueChangeEvent event) {		
 			if(canvas == null) return;			
 			
 			if(event.getProperty() == size){							
 				canvas.getInteractive().setToolSize(Double.parseDouble(event.getProperty().getValue().toString()));				
 			} 
 			else if(event.getProperty() == opacity){
 				canvas.getInteractive().setAlpha(1.0-Double.parseDouble(event.getProperty().getValue().toString())/100.0);
 			}
 		}
 
 		@Override
 		public String getName() {
 			return "Rectangle";
 		}
 
 		@Override
 		public void changed(ColorSelector selector, Color color) {
 			colorpicker.selectForegroundColorPicker();	
 			canvas.getInteractive().setColor(colorToHex(colorpicker.getColor()));	
 			
 			if(disableFillcolor.booleanValue()){
 				colorpicker.selectBackgroundColorPicker();
 				canvas.getInteractive().setFillColor(colorToHex(colorpicker.getColor()));	
 				canvas.getInteractive().setFillAlpha(1.0);
 			}
 		}
 
 		@Override
 		public void buttonClick(ClickEvent event) {
 			if(event.getButton() == disableFillcolor){
 				boolean state = event.getButton().booleanValue();
 				if(state){
 					colorpicker.selectBackgroundColorPicker();
 					canvas.getInteractive().setFillColor(colorToHex(colorpicker.getColor()));	
 					canvas.getInteractive().setFillAlpha(1.0);
 				} else {
 					canvas.getInteractive().setFillColor(null);
 					canvas.getInteractive().setFillAlpha(0.0);
 				}
 			}			
 		}
 
 		@Override
 		public void sendCurrentSettings() {
 			if(this.canvas == null) return;
 			
 			Interactive i = this.canvas.getInteractive();
 			
 			colorpicker.selectForegroundColorPicker();			
 			i.setColor(colorToHex(colorpicker.getColor()));
 			
 			i.setToolSize(Double.parseDouble(size.getValue().toString()));
 			
 			i.setAlpha(1.0-Double.parseDouble(opacity.getValue().toString())/100.0);		
 		
 			boolean state = disableFillcolor.booleanValue();
 			if(state){
 				colorpicker.selectBackgroundColorPicker();
 				i.setFillColor(colorToHex(colorpicker.getColor()));				
 			} else {
 				i.setFillColor(null);
 			}		
 		}
 }
