 package net.rptools.maptool.client.functions;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Area;
 import java.math.BigDecimal;
 import java.util.List;
 
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolVariableResolver;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.parser.Parser;
 import net.rptools.parser.ParserException;
 import net.rptools.parser.function.AbstractFunction;
 
 public class TokenSightFunctions extends AbstractFunction {
 
 	private static final TokenSightFunctions instance = new TokenSightFunctions();
 
 	private TokenSightFunctions() {
		super(0, 1, "hasSight", "setHasSight", "getSightType", "setSightType", "canSeeToken");
 	}
 
 	public static TokenSightFunctions getInstance() {
 		return instance;
 	}
 
 	private enum TokenLocations {
 		TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT, CENTER
 	}
 
 	@Override
 	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
 		Token tokenInContext = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
 		if (tokenInContext == null) {
 			throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
 		}
 		if (functionName.equals("hasSight")) {
 			return tokenInContext.getHasSight() ? BigDecimal.ONE : BigDecimal.ZERO;
 		}
 		if (functionName.equals("getSightType")) {
 			return tokenInContext.getSightType();
 		}
 		if (functionName.equals("setHasSight")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			}
 			tokenInContext.setHasSight(!parameters.get(0).equals(BigDecimal.ZERO));
 			MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(tokenInContext);
 			MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), tokenInContext);
 			MapTool.getFrame().getCurrentZoneRenderer().flushLight();
 			return "";
 		}
 		if (functionName.equals("setSightType")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			}
 			tokenInContext.setSightType(parameters.get(0).toString());
 			MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(tokenInContext);
 			MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), tokenInContext);
 			MapTool.getFrame().getCurrentZoneRenderer().flushLight();
 			return "";
 		}
 		if (functionName.equals("canSeeToken")) {
 			if (parameters.size() < 1) {
 				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
 			}
 			if (parameters.size() > 2) {
 				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 2, parameters.size()));
 			}
 			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
 			if (parameters.size() == 2) {
 				try {
 					tokenInContext = FindTokenFunctions.findToken(parameters.get(1).toString(), zone.getName());
 				} catch (Exception e) {
 					if (e instanceof ClassCastException || tokenInContext == null) {
 						throw new ParserException(I18N.getText("macro.function.general.argumentTypeT", 2, functionName));
 					}
 				}
 
 			}
 			if (!tokenInContext.getHasSight()) {
 				return "[]";
 			}
 			Area tokensVisibleArea = MapTool.getFrame().getCurrentZoneRenderer().getZoneView().getVisibleArea(tokenInContext);
 			if (tokensVisibleArea == null) {
 				return "[]";
 			}
 			Token target = null;
 			try {
 				target = FindTokenFunctions.findToken(parameters.get(0).toString(), zone.getName());
 			} catch (Exception e) {
 				if (e instanceof ClassCastException || tokenInContext == null) {
 					throw new ParserException(I18N.getText("macro.function.general.argumentTypeT", 2, functionName));
 				}
 			}
 			if (target == null) {
 				return "[]";
 			}
 			if (!target.isVisible() || (target.isVisibleOnlyToOwner() && !AppUtil.playerOwns(target))) {
 				return "[]";
 			}
 			Grid grid = zone.getGrid();
 			target.getFootprint(zone.getGrid());
 
 			Rectangle bounds = target.getFootprint(grid).getBounds(grid, grid.convert(new ZonePoint(target.getX(), target.getY())));
 
 			int x = (int) bounds.getX();
 			int y = (int) bounds.getY();
 			int w = (int) bounds.getWidth();
 			int h = (int) bounds.getHeight();
 
 			StringBuilder sb = new StringBuilder();
 			sb.append("[");
 
 			int halfX = x + (w) / 2;
 			int halfY = y + (h) / 2;
 			if (tokensVisibleArea.intersects(bounds)) {
 				if (tokensVisibleArea.contains(new Point(x, y))) {
 					//TOP_LEFT
 					sb.append("\"");
 					sb.append(TokenLocations.TOP_LEFT.toString());
 					sb.append("\", ");
 				}
 				if (tokensVisibleArea.contains(new Point(x, y + h))) {
 					//BOTTOM_LEFT
 					sb.append("\"");
 					sb.append(TokenLocations.BOTTOM_LEFT.toString());
 					sb.append("\", ");
 				}
 				if (tokensVisibleArea.contains(new Point(x + w, y))) {
 					//TOP_RIGHT
 					sb.append("\"");
 					sb.append(TokenLocations.TOP_RIGHT.toString());
 					sb.append("\", ");
 				}
 				if (tokensVisibleArea.contains(new Point(x + w, y + h))) {
 					//BOTTOM_RIGHT
 					sb.append("\"");
 					sb.append(TokenLocations.BOTTOM_RIGHT.toString());
 					sb.append("\", ");
 				}
 				if (tokensVisibleArea.contains(new Point(halfX, halfY))) {
 					//BOTTOM_RIGHT
 					sb.append("\"");
 					sb.append(TokenLocations.CENTER.toString());
 					sb.append("\", ");
 				}
 			}
 			if (sb.length() > 2 && sb.lastIndexOf(", ") == sb.length() - 2) {
 				sb.replace(sb.length() - 2, sb.length(), "");
 			}
 			sb.append("]");
 
 			return sb.toString();
 		}
 		return "";
 	}
 }
