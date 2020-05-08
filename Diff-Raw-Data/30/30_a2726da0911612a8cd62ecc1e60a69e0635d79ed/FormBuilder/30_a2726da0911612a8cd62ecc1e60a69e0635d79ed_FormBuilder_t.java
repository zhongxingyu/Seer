 package de.htwg.mgse.formular.dsl;
 
 import de.htwg.mgse.formular.model.Formular;
 import de.htwg.mgse.formular.model.Input;
 import de.htwg.mgse.formular.model.InputType;
 
 public class FormBuilder {
 
 	private FormBuilder() {
 	}
 
 	public static FormScope form(String formId) {
 		Formular formular = new Formular(formId);
 		FormScope fscope = new FormScope(formular);
 		return fscope;
 	}
 
 	public static class FormScope {
 		private final Formular formular;
 
		private FormScope(Formular formular) {
 			this.formular = formular;
 		}
 
 		public InputScopeLabel input(String inputId) {
 			return new InputScopeLabel(this, new Input(inputId));
 		}
 		
 		public Formular generate() {
 			return formular;
 		}
 
 		public static class InputScopeLabel {
 			private final FormScope fs;
 			private final Input input;
 
			private InputScopeLabel(FormScope fs, Input input) {
 				this.fs = fs;
 				this.input = input;
 			}
 
 			public InputScopeType label(String label) {
				input.setLabel(label);
				return new InputScopeType(fs, input);
 			}
 
 			public static class InputScopeType {
 				private final Input input;
 				private final FormScope fs;
 
				private InputScopeType(FormScope fs, Input input) {
 					this.fs = fs;
 					this.input = input;
 				}
 
 				public InputScopeDefaultValue type(InputType type) {
					input.setType(type);
					return new InputScopeDefaultValue(fs, input);
 				}
 
 				public static class InputScopeDefaultValue {
 					private final FormScope fs;
 					private final Input input;
 
					private InputScopeDefaultValue(FormScope fs, Input input) {
 						this.fs = fs;
 						this.input = input;
 					}
 
 					public FormScope defaultValue(String defaultValue) {
						input.setDefaultValue(defaultValue);
 						fs.formular.addElement(input);
 						return fs;
 					}
 				}
 			}
 		}
 	}
 }
