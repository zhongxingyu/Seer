 package com.mgs.fantasi.ui.wireframe;
 
 import java.util.List;
 
 public class Layers<T> implements Structure {
 	private final List<T> layers;
 
 	public Layers(List<T> layers) {
 		this.layers = layers;
 	}
 
	public List<T> asList() {
		return layers;
	}

 	public void iterateInCrescendo(LayerIterator layerIterator) {
 		int zIndex = 0;
		for (T layer : layers) {
			layerIterator.on (zIndex, layer, new Margin());
 			zIndex++;
 		}
 	}
 }
