 /***************************************************************************
  * Copyright (C) 2011  Philippe Leipold
  *
  * This file is part of SimpleCalc.
  *
  * SimpleCalc is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleCalc is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleCalc. If not, see <http://www.gnu.org/licenses/>.
  *
  ***************************************************************************/
 
 package de.Lathanael.SimpleCalc.Window;
 
 import java.text.DecimalFormat;
 
 import org.bukkit.ChatColor;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.TextField;
 import org.getspout.spoutapi.gui.Texture;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import de.Lathanael.SimpleCalc.SimpleCalc;
 import de.Lathanael.SimpleCalc.Exceptions.MathSyntaxMismatch;
 import de.Lathanael.SimpleCalc.Parser.MathExpParser;
 
 /**
 * @author Lathanael (aka Philippe Leipold)
 * https://github.com/Lathanael
 **/
 
 public class CalcWindow extends GenericPopup {
 	private Texture background;
 	private Geometry edges = new Geometry();
 	private TextField expression, result;
 	private Label label;
 	private String title = "SimpleCalc";
 	private SpoutPlayer player;
 	private Button one, two, three, four, five, six, seven, eight, nine, zero;
 	private Button plus, minus, divide, power, multiply, remainder, leftParan, rightParan, comma, equal, ac, del, sqrt;
 	private Button close, hide;
 	private DecimalFormat format = new DecimalFormat("#0.00");
 
 	public CalcWindow (SpoutPlayer player, SimpleCalc plugin) {
 		this.player = player;
 		this.background = new GenericTexture("http://dl.dropbox.com/u/42731731/CalcBackground.png");
 		int screenWidth = player.getMainScreen().getWidth();
 		int screenHeight = player.getMainScreen().getHeight();
 		background.setHeight(166).setWidth(100).setX((screenWidth - 100)/2).setY((screenHeight-166)/2);
 		background.setPriority(RenderPriority.Highest);
 		edges.setLeft((screenWidth-90)/2);
 		edges.setRight(background.getX()+background.getWidth()-10);
 		edges.setTop(background.getY()+10);
 		edges.setBottom(background.getHeight()+background.getY()-10);
 		label = new GenericLabel(ChatColor.GREEN + title);
 		label.setHeight(10).setWidth(40).setX(edges.getLeft()).setY(edges.getTop());
 		attachWidget(plugin, label);
 		attachWidget(plugin, background);
 		close = new GenericButton(ChatColor.WHITE + "X");
 		close.setAlign(WidgetAnchor.CENTER_CENTER);
 		close.setHeight(10).setWidth(10).setX(edges.getRight()-5).setY(edges.getTop());
 		close.setTooltip("Close the Window");
 		attachWidget(plugin, close);
 		hide = new GenericButton(ChatColor.WHITE + "_");
 		hide.setAlign(WidgetAnchor.CENTER_CENTER);
 		hide.setHeight(10).setWidth(10).setX(edges.getRight()-15).setY(edges.getTop());
 		hide.setTooltip("Hide the Window");
 		attachWidget(plugin, hide);
 		one = new GenericButton(ChatColor.WHITE + "1");
 		one.setAlign(WidgetAnchor.CENTER_CENTER);
 		one.setHeight(10).setWidth(10).setX(edges.getLeft()).setY(edges.getTop() + 75);
 		attachWidget(plugin, one);
 		two = new GenericButton(ChatColor.WHITE + "2");
 		two.setAlign(WidgetAnchor.CENTER_CENTER);
 		two.setHeight(10).setWidth(10).setX(edges.getLeft() + 15).setY(edges.getTop() + 75);
 		attachWidget(plugin, two);
 		three = new GenericButton(ChatColor.WHITE + "3");
 		three.setAlign(WidgetAnchor.CENTER_CENTER);
 		three.setHeight(10).setWidth(10).setX(edges.getLeft() + 30).setY(edges.getTop() + 75);
 		attachWidget(plugin, three);
 		four = new GenericButton(ChatColor.WHITE + "4");
 		four.setAlign(WidgetAnchor.CENTER_CENTER);
 		four.setHeight(10).setWidth(10).setX(edges.getLeft() + 45).setY(edges.getTop() + 75);
 		attachWidget(plugin, four);
 		five = new GenericButton(ChatColor.WHITE + "5");
 		five.setAlign(WidgetAnchor.CENTER_CENTER);
 		five.setHeight(10).setWidth(10).setX(edges.getLeft() + 60).setY(edges.getTop() + 75);
 		attachWidget(plugin, five);
 		six = new GenericButton(ChatColor.WHITE + "6");
 		six.setAlign(WidgetAnchor.CENTER_CENTER);
 		six.setHeight(10).setWidth(10).setX(edges.getLeft()).setY(edges.getTop() + 90);
 		attachWidget(plugin, six);
 		seven = new GenericButton(ChatColor.WHITE + "7");
 		seven.setAlign(WidgetAnchor.CENTER_CENTER);
 		seven.setHeight(10).setWidth(10).setX(edges.getLeft() + 15).setY(edges.getTop() + 90);
 		attachWidget(plugin, seven);
 		eight = new GenericButton(ChatColor.WHITE + "8");
 		eight.setAlign(WidgetAnchor.CENTER_CENTER);
 		eight.setHeight(10).setWidth(10).setX(edges.getLeft() + 30).setY(edges.getTop() + 90);
 		attachWidget(plugin, eight);
 		nine = new GenericButton(ChatColor.WHITE + "9");
 		nine.setAlign(WidgetAnchor.CENTER_CENTER);
 		nine.setHeight(10).setWidth(10).setX(edges.getLeft() + 45).setY(edges.getTop() + 90);
 		attachWidget(plugin, nine);
 		zero = new GenericButton(ChatColor.WHITE + "0");
 		zero.setAlign(WidgetAnchor.CENTER_CENTER);
 		zero.setHeight(10).setWidth(10).setX(edges.getLeft() + 60).setY(edges.getTop() + 90);
 		attachWidget(plugin, zero);
 		comma = new GenericButton(ChatColor.WHITE + ",");
 		comma.setAlign(WidgetAnchor.CENTER_CENTER);
 		comma.setHeight(10).setWidth(10).setX(edges.getLeft()).setY(edges.getTop() + 105);
 		attachWidget(plugin, comma);
 		equal = new GenericButton(ChatColor.WHITE + "=");
 		equal.setAlign(WidgetAnchor.CENTER_CENTER);
 		equal.setHeight(10).setWidth(10).setX(edges.getLeft() + 15).setY(edges.getTop() + 105);
 		attachWidget(plugin, equal);
 		ac = new GenericButton(ChatColor.WHITE + "AC");
 		ac.setAlign(WidgetAnchor.CENTER_CENTER);
 		ac.setHeight(10).setWidth(20).setX(edges.getLeft() + 30).setY(edges.getTop() + 105);
 		attachWidget(plugin, ac);
 		del = new GenericButton(ChatColor.WHITE + "DEL");
 		del.setAlign(WidgetAnchor.CENTER_CENTER);
 		del.setHeight(10).setWidth(25).setX(edges.getLeft() + 55).setY(edges.getTop() + 105);
 		attachWidget(plugin, del);
 		plus = new GenericButton(ChatColor.WHITE + "+");
 		plus.setAlign(WidgetAnchor.CENTER_CENTER);
 		plus.setHeight(10).setWidth(10).setX(edges.getLeft()).setY(edges.getTop() + 120);
 		attachWidget(plugin, plus);
 		minus = new GenericButton(ChatColor.WHITE + "-");
 		minus.setAlign(WidgetAnchor.CENTER_CENTER);
 		minus.setHeight(10).setWidth(10).setX(edges.getLeft() + 15).setY(edges.getTop() + 120);
 		attachWidget(plugin, minus);
 		multiply = new GenericButton(ChatColor.WHITE + "*");
 		multiply.setAlign(WidgetAnchor.CENTER_CENTER);
 		multiply.setHeight(10).setWidth(10).setX(edges.getLeft() + 30).setY(edges.getTop() + 120);
 		attachWidget(plugin, multiply);
 		divide = new GenericButton(ChatColor.WHITE + "/");
 		divide.setAlign(WidgetAnchor.CENTER_CENTER);
 		divide.setHeight(10).setWidth(10).setX(edges.getLeft() + 45).setY(edges.getTop() + 120);
 		attachWidget(plugin, divide);
 		power = new GenericButton(ChatColor.WHITE + "^");
 		power.setAlign(WidgetAnchor.CENTER_CENTER);
 		power.setHeight(10).setWidth(10).setX(edges.getLeft()).setY(edges.getTop() + 135);
 		attachWidget(plugin, power);
 		remainder = new GenericButton(ChatColor.WHITE + "%");
 		remainder.setAlign(WidgetAnchor.CENTER_CENTER);
 		remainder.setHeight(10).setWidth(10).setX(edges.getLeft() + 15).setY(edges.getTop() + 135);
 		attachWidget(plugin, remainder);
 		leftParan = new GenericButton(ChatColor.WHITE + "(");
 		leftParan.setAlign(WidgetAnchor.CENTER_CENTER);
 		leftParan.setHeight(10).setWidth(10).setX(edges.getLeft() + 30).setY(edges.getTop() + 135);
 		attachWidget(plugin, leftParan);
 		rightParan = new GenericButton(ChatColor.WHITE + ")");
 		rightParan.setAlign(WidgetAnchor.CENTER_CENTER);
 		rightParan.setHeight(10).setWidth(10).setX(edges.getLeft() + 45).setY(edges.getTop() + 135);
 		attachWidget(plugin, rightParan);
 		sqrt = new GenericButton(ChatColor.WHITE + "√");
 		sqrt.setAlign(WidgetAnchor.CENTER_CENTER);
 		sqrt.setVisible(false);
 		initialisePopup();
 	}
 
 	public Geometry getGeometry() {
 		return edges;
 	}
 
 	public void initialisePopup() {
 		SimpleCalc plugin = SimpleCalc.getInstance();
 		expression = new GenericTextField();
 		result = new GenericTextField();
 		result.setHeight(20).setWidth(90).setX(edges.getLeft()).setY(edges.getTop() + 50);
 		expression.setHeight(20).setWidth(90).setX(edges.getLeft()).setY(edges.getTop() + 25);
 		attachWidget(plugin, result);
 		attachWidget(plugin, expression);
 	}
 
 	public void open(){
 		player.getMainScreen().attachPopupScreen(this);
 		setDirty(true);
 		for(Widget widget : getAttachedWidgets()) {
 			widget.setDirty(true);
 			widget.setVisible(true);
 		}
 	}
 
 	public void hide(){
 		close();
 	}
 
 	public void closeWindow() {
 		SimpleCalc plugin = SimpleCalc.getInstance();
 		plugin.removePopup(player);
 	}
 
 	public void onClick(Button button) {
 		if (button.equals(close)) {
 			SimpleCalc.getInstance().removePopup(player);
 			close();
 		}
 		else if (button.equals(hide))
 			hide();
 		else if (button.equals(equal)) {
 			String calc = expression.getText();
 			calc = calc.replaceAll(" ", "");
 			calc = calc.replaceAll(",", ".");
 			try {
 				MathExpParser eqaution = new MathExpParser(calc, player.getName());
 				double result = eqaution.compute();
 				this.result.setText(format.format(result));
 				this.result.setDirty(true);
 			}
 			// The equation given is incorrect!
 			catch(MathSyntaxMismatch mismatch){
 				this.result.setText(ChatColor.RED + mismatch.getMessage());
 				this.result.setDirty(true);
 			}
 		}
 		else if (button.equals(one)) {
 			expression.setText(expression.getText() + "1");
 			expression.setDirty(true);
 		}
 		else if (button.equals(two)) {
 			expression.setText(expression.getText() + "2");
 			expression.setDirty(true);
 		}
 		else if (button.equals(three)) {
 			expression.setText(expression.getText() + "3");
 			expression.setDirty(true);
 		}
 		else if (button.equals(four)) {
 			expression.setText(expression.getText() + "4");
 			expression.setDirty(true);
 		}
 		else if (button.equals(five)) {
 			expression.setText(expression.getText() + "5");
 			expression.setDirty(true);
 		}
 		else if (button.equals(six)) {
 			expression.setText(expression.getText() + "6");
 			expression.setDirty(true);
 		}
 		else if (button.equals(seven)) {
 			expression.setText(expression.getText() + "7");
 			expression.setDirty(true);
 		}
 		else if (button.equals(eight)) {
 			expression.setText(expression.getText() + "8");
 			expression.setDirty(true);
 		}
 		else if (button.equals(nine)) {
 			expression.setText(expression.getText() + "9");
 			expression.setDirty(true);
 		}
 		else if (button.equals(zero)) {
 			expression.setText(expression.getText() + "0");
 			expression.setDirty(true);
 		}
 		else if (button.equals(plus)) {
 			expression.setText(expression.getText() + "+");
 			expression.setDirty(true);
 		}
 		else if (button.equals(minus)) {
 			expression.setText(expression.getText() + "-");
 			expression.setDirty(true);
 		}
 		else if (button.equals(multiply)) {
 			expression.setText(expression.getText() + "*");
 			expression.setDirty(true);
 		}
 		else if (button.equals(divide)) {
 			expression.setText(expression.getText() + "/");
 			expression.setDirty(true);
 		}
 		else if (button.equals(power)) {
 			expression.setText(expression.getText() + "^");
 			expression.setDirty(true);
 		}
 		else if (button.equals(remainder)) {
 			expression.setText(expression.getText() + "%");
 			expression.setDirty(true);
 		}
 		else if (button.equals(comma)) {
 			expression.setText(expression.getText() + ",");
 			expression.setDirty(true);
 		}
 		else if (button.equals(ac)) {
 			expression.setText("");
 			expression.setDirty(true);
 		}
 		else if (button.equals(leftParan)) {
 			expression.setText(expression.getText() + "(");
 			expression.setDirty(true);
 		}
 		else if (button.equals(rightParan)) {
 			expression.setText(expression.getText() + ")");
 			expression.setDirty(true);
 		}
 		else if (button.equals(del)) {
 			String text = expression.getText();
			text = text.substring(0, text.length()-1);
 			expression.setText(text);
 			expression.setDirty(true);
 		}
 	}
 }
