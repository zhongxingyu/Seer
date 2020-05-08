 package org.dawnsci.common.widgets.gda.function.internal;
 
 import org.dawnsci.common.widgets.gda.Activator;
 import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
 import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
 import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
 import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
 import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
 import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
 import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionError;
 import org.eclipse.jface.preference.JFacePreferences;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.BaseLabelProvider;
 import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.StyledString.Styler;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.TextStyle;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;
 
 public class FunctionLabelProvider extends BaseLabelProvider implements
 		IStyledLabelProvider {
 
 	private static final Image CURVE = Activator.getImage("chart_curve.png");
 	private static final Image LINK = Activator.getImage("link.png");
 	private static final Image BULLET_BLUE = Activator
 			.getImage("bullet_blue.png");
 	private static final Image BULLET_ORANGE = Activator
 			.getImage("bullet_orange.png");
 	public static final Styler ERROR_STYLER = new Styler() {
 		@Override
 		public void applyStyles(TextStyle textStyle) {
 			textStyle.foreground = JFaceResources.getColorRegistry().get(
 					JFacePreferences.ERROR_COLOR);
 		}
 	};
 
 	@Override
 	public Image getImage(Object element) {
 		if (element instanceof AddNewFunctionModel) {
 			return PlatformUI.getWorkbench().getSharedImages()
 					.getImage(ISharedImages.IMG_OBJ_ADD);
 		} else if (element instanceof SetFunctionModel) {
 			return PlatformUI.getWorkbench().getSharedImages()
 					.getImage(ISharedImages.IMG_OBJ_ADD);
 		} else if (element instanceof FunctionModel) {
 			if (((FunctionModel)element).getFunction() instanceof JexlExpressionFunction)
 				return LINK;
 			return CURVE;
 		} else if (element instanceof OperatorModel) {
 			return BULLET_BLUE;
 		} else if (element instanceof ParameterModel) {
 			return BULLET_ORANGE;
 		}
 
 		return null;
 	}
 
 	@Override
 	public StyledString getStyledText(Object element) {
 		if (element instanceof AddNewFunctionModel) {
 			return new StyledString("Add new function",
 					StyledString.COUNTER_STYLER);
 		} else if (element instanceof SetFunctionModel) {
 			return new StyledString("Set function", StyledString.COUNTER_STYLER);
 		} else if (element instanceof FunctionModel) {
 			FunctionModel functionModel = (FunctionModel) element;
 			IFunction function = functionModel.getFunction();
 			if (function instanceof JexlExpressionFunction) {
 				return getJexlStyledText((JexlExpressionFunction) function);
 			} else {
 				return new StyledString(function.getName());
 			}
 		} else if (element instanceof OperatorModel) {
 			OperatorModel operatorModel = (OperatorModel) element;
 			IOperator operator = operatorModel.getOperator();
 			return new StyledString(operator.getName());
 		} else if (element instanceof ParameterModel) {
 			ParameterModel parameterModel = (ParameterModel) element;
 			return new StyledString(parameterModel.getParameter().getName());
 		}
 
 		return new StyledString("");
 	}
 
 	public StyledString getJexlStyledText(
 			JexlExpressionFunction jexlExpressionFunction) {
 		String expression = jexlExpressionFunction.getExpression();
 		if (expression == null)
 			expression = "";
 		if (jexlExpressionFunction.getExpressionError() == JexlExpressionFunctionError.NO_ERROR) {
 			return new StyledString(expression);
 		} else {
 			return new StyledString(expression, ERROR_STYLER);
 		}
 	}
 
 }
