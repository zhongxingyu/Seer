 /**
  * Copyright (C) 2012-2014 Philip W. Sorst <philip@sorst.net>
  * and individual contributors as indicated
  * by the @authors tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.dontdrinkandroot.wicket.bootstrap.behavior;
 
 import net.dontdrinkandroot.wicket.behavior.CssClassAppender;
 import net.dontdrinkandroot.wicket.bootstrap.css.BootstrapCssClass;
 import net.dontdrinkandroot.wicket.bootstrap.css.ButtonSize;
 import net.dontdrinkandroot.wicket.bootstrap.css.ButtonStyle;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 
 public class ButtonBehavior extends Behavior {
 
	private IModel<ButtonStyle> buttonStyleModel = new Model<ButtonStyle>();
 
 	private IModel<ButtonSize> buttonSizeModel = new Model<ButtonSize>();
 
 
 	public ButtonBehavior() {
 
 		super();
 	}
 
 
 	public ButtonBehavior(IModel<ButtonStyle> buttonStyleModel, IModel<ButtonSize> buttonSizeModel) {
 
 		this.buttonStyleModel = buttonStyleModel;
 		this.buttonSizeModel = buttonSizeModel;
 	}
 
 
 	@Override
 	public void bind(Component component) {
 
 		super.bind(component);
 
 		component.add(new CssClassAppender(BootstrapCssClass.BTN));
 		component.add(new CssClassAppender(this.getButtonStyleModel()));
 		component.add(new CssClassAppender(this.getButtonSizeModel()));
 	}
 
 
 	public ButtonSize getButtonSize() {
 
 		return this.getButtonSizeModel().getObject();
 	}
 
 
 	public ButtonBehavior setButtonSize(ButtonSize buttonSize) {
 
 		this.getButtonSizeModel().setObject(buttonSize);
 		return this;
 	}
 
 
 	public ButtonStyle getButtonStyle() {
 
 		return this.getButtonStyleModel().getObject();
 	}
 
 
 	public ButtonBehavior setButtonStyle(ButtonStyle buttonStyle) {
 
 		this.getButtonStyleModel().setObject(buttonStyle);
 		return this;
 	}
 
 
 	protected IModel<ButtonSize> getButtonSizeModel() {
 
 		return this.buttonSizeModel;
 	}
 
 
 	protected IModel<ButtonStyle> getButtonStyleModel() {
 
 		return this.buttonStyleModel;
 	}
 
 }
