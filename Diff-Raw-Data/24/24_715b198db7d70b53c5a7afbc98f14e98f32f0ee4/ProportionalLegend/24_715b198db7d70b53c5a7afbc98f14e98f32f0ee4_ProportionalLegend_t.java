 package org.gvsig.legend.impl;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.styling.Symbolizer;
 import org.gvsig.layer.Layer;
 import org.gvsig.legend.Interval;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.filter.Filter;
 import org.opengis.filter.expression.Expression;
 import org.opengis.filter.expression.Literal;
 import org.opengis.filter.expression.PropertyName;
 
 import com.google.inject.assistedinject.Assisted;
 
 public class ProportionalLegend extends AbstractLegend {
 	private String valueField, normalizationField;
 	private Interval size;
 	private Symbolizer template, background;
 	private Symbolizer[] symbols;
 	private boolean useBackground;
 
 	private Expression sizeExp;
 
 	@Inject
 	public ProportionalLegend(@Assisted Layer layer,
 			@Assisted("value") String valueField,
 			@Assisted("normalization") String normalizationField,
 			@Assisted("template") Symbolizer template,
 			@Assisted("background") Symbolizer background,
 			@Assisted boolean useBackground, @Assisted Interval size) {
 		super(layer);
 		this.valueField = valueField;
 		this.normalizationField = normalizationField;
 		this.size = size;
 		this.template = template;
 		this.background = background;
 		this.useBackground = useBackground;
 	}
 
 	public String getValueField() {
 		return valueField;
 	}
 
 	public String getNormalizationField() {
 		return normalizationField;
 	}
 
 	public Interval getSize() {
 		return size;
 	}
 
 	public Symbolizer getTemplate() {
 		return template;
 	}
 
 	public Symbolizer getBackground() {
 		return background;
 	}
 
 	@Override
 	protected Symbolizer[] getSymbols(Object key) {
 		return null;
 	}
 
 	@Override
 	protected Object[] getSymbolizerKeys() {
 		return new Object[0];
 	}
 
 	@Override
 	protected Filter getFilter(Object key) {
 		return null;
 	}
 
 	@Override
 	protected Symbolizer[] getSymbolsForElseFilter() throws IOException {
 		if (symbols == null) {
 			createSymbols();
			ResizeCopyStyleVisitor visitor = new ResizeCopyStyleVisitor(
					sizeExp, styleFactory, filterFactory);
 			template.accept(visitor);
 			Symbolizer symbol = (Symbolizer) visitor.getCopy();
 			if (useBackground) {
 				symbols = new Symbolizer[] { background, symbol };
 			} else {
 				symbols = new Symbolizer[] { symbol };
 			}
 		}
 
 		return symbols;
 
 	}
 
 	private void createSymbols() throws IOException {
 		boolean useNormalization = normalizationField != null
 				&& !normalizationField.equals(valueField);
 
 		double maxValue = Double.NEGATIVE_INFINITY;
 		double minValue = Double.MAX_VALUE;
 		double valueRange = 0;
 
 		if (!useNormalization) {
 			SimpleFeatureIterator iterator = layer.getFeatureSource()
 					.getFeatures().features();
 			while (iterator.hasNext()) {
 				SimpleFeature feature = iterator.next();
 				double value = Double.parseDouble(feature.getAttribute(
 						normalizationField).toString());
 				if (value > maxValue) {
 					maxValue = value;
 				}
 				if (value < minValue) {
 					minValue = value;
 				}
 			}
 			valueRange = maxValue - minValue;
 		}
 
 		Literal min = filterFactory.literal(size.getMin());
 		Literal sizeRange = filterFactory
 				.literal(size.getMax() - size.getMin());
 		PropertyName value = filterFactory.property(valueField);
 		PropertyName normalization = filterFactory.property(normalizationField);
 
 		if (useNormalization) {
 			// min + (value * range / normalization)
 			Expression exp = filterFactory.divide(sizeRange, normalization);
 			exp = filterFactory.multiply(value, exp);
 			this.sizeExp = filterFactory.add(min, exp);
 		} else {
 			// min + ((value - min) * sizeRange / valueRange)
 			Expression exp = filterFactory.subtract(value, min);
 			exp = filterFactory.multiply(exp, sizeRange);
 			exp = filterFactory.divide(exp, filterFactory.literal(valueRange));
 			this.sizeExp = filterFactory.add(min, exp);
 		}
 	}
 }
