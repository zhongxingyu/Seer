 package com.mgs.invasion;
 
 import com.mgs.fantasi.polygon.HexagonShape;
import com.mgs.fantasi.structures.*;
 import com.mgs.fantasi.ui.driver.UIDriver;
 import com.mgs.fantasi.ui.profile.ExpandToParent;
 import com.mgs.fantasi.ui.profile.UIProfileFactory;
 import com.mgs.fantasi.ui.wireframe.Wireframe;
import com.mgs.invasion.mvc.view.ProductionUiProfileFactory;
 
 import javax.swing.*;
 import java.awt.*;
 
 import static com.mgs.fantasi.structures.LayeredElementStructureBuilder.layered;
 import static com.mgs.fantasi.structures.PijamaRowsStructureBuilder.pijamaRows;
 import static com.mgs.fantasi.structures.PolygonStructureBuilder.polygonWithShape;
 import static com.mgs.fantasi.structures.RectangleStructureBuilder.emptyRectangle;
 import static com.mgs.fantasi.structures.VerticalSlicesStructureBuilder.verticalSlices;
 
 public class Main {
 	public static void main(String... args) {
//		new Main().go(new DebugUIProfileFactory());
		new Main().go(new ProductionUiProfileFactory());
 	}
 
 	private void go(UIProfileFactory uiProfileFactory) {
 		UIDriver<JPanel> uiDriver = UIDriver.forSwing(uiProfileFactory.getUIProfile());
 
 		Wireframe content =
 			layered().
 				withLayer(
 					pijamaRows(
 						verticalSlices(
 							emptyRectangle().
 								withContent(
 									polygonWithShape(new HexagonShape()).
 									withSizeStrategy(new ExpandToParent())
 								)
 						).
 						withVerticalDivisions(10),
 						emptyRectangle()
 					).
 					withFirstRowSize(Fractions.thwoThirds()).
 					withNumberOfGerations(5)
 				).
 				withLayer(
 					emptyRectangle()
 				).
 			build();
 
 		uiDriver.show(content, new Dimension(400, 400));
 	}
 
 }
