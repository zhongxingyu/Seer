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
 
		public FormScope(Formular formular) {
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
 
			public InputScopeLabel(FormScope fs, Input input) {
 				this.fs = fs;
 				this.input = input;
 			}
 
 			public InputScopeType label(String label) {
				return new InputScopeType(fs, input, label);
 			}
 
 			public static class InputScopeType {
				private final String label;
 				private final Input input;
 				private final FormScope fs;
 
				public InputScopeType(FormScope fs, Input input, String label) {
 					this.fs = fs;
 					this.input = input;
					this.label = label;
 				}
 
 				public InputScopeDefaultValue type(InputType type) {
					return new InputScopeDefaultValue(fs, input, type);
 				}
 
 				public static class InputScopeDefaultValue {
 					private final FormScope fs;
 					private final Input input;
					private final InputType type;
 
					public InputScopeDefaultValue(FormScope fs, Input input,
							InputType type) {
 						this.fs = fs;
 						this.input = input;
						this.type = type;
 					}
 
 					public FormScope defaultValue(String defaultValue) {
 						fs.formular.addElement(input);
 						return fs;
 					}
 				}
 			}
 		}
 	}
 }
