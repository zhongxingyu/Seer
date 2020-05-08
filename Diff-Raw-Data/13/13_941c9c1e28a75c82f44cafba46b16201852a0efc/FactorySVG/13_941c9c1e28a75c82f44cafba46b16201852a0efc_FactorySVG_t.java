 package rendering.svg;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import model.Instruction;
 import model.Model;
 import model.Picture;
 import model.instruction.Function;
 import model.instruction.Operator;
 import model.instruction.functions.DrawPath;
 import model.instruction.functions.DrawSmiley;
 import model.instruction.operators.For;
 import model.instruction.operators.Ifelse;
 import model.variables.Bezier;
 import model.variables.Circle;
 import model.variables.Path;
 import model.variables.Polygon;
 
 import rendering.svg.renderers.DrawBezierSvg;
 import rendering.svg.renderers.DrawCircleSvg;
 import rendering.svg.renderers.DrawPolygoneSvg;
import rendering.svg.renderers.DrawSmileySVG;
 import rendering.svg.renderers.ForSVG;
 import rendering.svg.renderers.IfElseSVG;
 
 
 
 /**
  * @author Andres Felipe Gutierrez, Amaury Ollagnier et Dimitri Justeau
  * @version 1.0
  * @created 20-oct.-2012 14:32:49
  */
 public class FactorySVG {
 
 	/**
 	 * Le constructueur
 	 */
 	public FactorySVG(){}
 
 	/**
 	 * Methode de factory
 	 * @param model
 	 * @return tous les renderer SVGgraphics2D lies aux donnees du modele
 	 * classe par picture dans une LinkedHashMap
 	 */
 	public static Map<Picture, PaneGraphicsSVG> makeRendererGraphicsSVG(Model model){
 		Map<Picture, PaneGraphicsSVG> map = 
 				new LinkedHashMap<Picture, PaneGraphicsSVG>();
 		for(Picture pic : model.getListPictures())
 			makeRendererGraphicsSVG(pic, map);
 		return map;
 	}
 
 	/**
 	 * Genere la liste des renderer pour une picture donnee
 	 * @param pic
 	 * @param map
 	 */
 	private static void makeRendererGraphicsSVG(Picture pic,
 			Map<Picture, PaneGraphicsSVG> map) {
 		PaneGraphicsSVG panel = new PaneGraphicsSVG(pic.getName());
 		for(Instruction ins : pic.getInstructions())
 			panel.addRenderer(makeRendererFrom(ins));
 		map.put(pic, panel);
 	}
 
 	/**
 	 * Methode qui va retourner le renderer SVG
 	 * associe a l'instruction en parametre du modele.
 	 * @param ins
 	 * @return
 	 */
 	public static RendererSVG makeRendererFrom(Instruction ins) {
 		if ( ins instanceof Operator){
 			Operator op = (Operator) ins;
 			if (op instanceof Ifelse)
 				return new IfElseSVG((Ifelse) op);
 			else if (op instanceof For)
 				return new ForSVG((For) op);
 		} 
 		
 		else if (ins instanceof Function){
 			
 			if (ins instanceof DrawPath){
 				Path path = ((DrawPath)ins).getPath();
 				if( path instanceof Circle )
 					return new DrawCircleSvg((DrawPath)ins);
 				else if ( path instanceof Polygon )
 					return new DrawPolygoneSvg((DrawPath) ins);
 				else if ( path instanceof Bezier )
 					return new DrawBezierSvg((DrawPath) ins);
 			} 
 			
 			else if ( ins instanceof DrawSmiley ){
				new DrawSmileySVG(ins);
 			}
 		}
 		return null;
 	}
 
 
 }
