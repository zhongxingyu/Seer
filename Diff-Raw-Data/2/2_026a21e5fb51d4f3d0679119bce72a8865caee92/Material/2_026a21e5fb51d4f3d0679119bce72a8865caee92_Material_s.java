 package com.github.neothemachine.glgegwt.client;
 
 public final class Material extends Events {
 
 	protected Material() {}
 	
 	public native Material getMaterial(int idx) /*-{
 		this.getMaterial(idx);
 	}-*/;
 	
 	/**
 	 * TODO This should accept Texture, TextureCamera, TextureCameraCube,
 	 *      TextureCube, TextureVideo, TextureCanvas but then we'd have to
 	 *      introduce a new supertype as they don't have one.
 	 * @param texture
 	 */
 	public native void addTexture(TextureCanvas texture) /*-{
 		this.addTexture(texture);
 	}-*/;
 	
 	public native void addMaterialLayer(MaterialLayer layer) /*-{
		this.addTexture(layer);
 	}-*/;
 	
 }
