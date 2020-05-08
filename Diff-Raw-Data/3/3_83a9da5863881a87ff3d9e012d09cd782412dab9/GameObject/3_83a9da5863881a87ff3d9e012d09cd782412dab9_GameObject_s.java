 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.framework;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.XmlWriter;
 import com.nebula2d.editor.common.IBuildable;
 import com.nebula2d.editor.common.IRenderable;
 import com.nebula2d.editor.common.ISelectable;
 import com.nebula2d.editor.common.ISerializable;
 import com.nebula2d.editor.framework.components.Behaviour;
 import com.nebula2d.editor.framework.components.Collider;
 import com.nebula2d.editor.framework.components.Component;
 import com.nebula2d.editor.framework.components.MusicSource;
 import com.nebula2d.editor.framework.components.Renderer;
 import com.nebula2d.editor.framework.components.RigidBody;
 import com.nebula2d.editor.framework.components.SoundEffectSource;
 import com.nebula2d.editor.framework.components.SpriteRenderer;
 import com.nebula2d.editor.framework.components.TileMapRenderer;
 import com.nebula2d.editor.util.FullBufferedReader;
 import com.nebula2d.editor.util.FullBufferedWriter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 public class GameObject extends BaseSceneNode implements ISerializable, IBuildable {
 
     protected Vector2 pos;
     protected  Vector2 scale;
     protected float rot;
 
     protected List<Component> components;
     protected Renderer renderer;
     protected RigidBody rigidBody;
     protected List<ISelectable> selectables;
     protected List<IRenderable> renderables;
 
     public GameObject(String name) {
         super(name);
         components = new ArrayList<>();
         renderables = new ArrayList<>();
         selectables = new ArrayList<>();
         pos = new Vector2();
         scale = new Vector2(1, 1);
         rot = 0;
     }
 
     public Vector2 getPosition() {
         return pos;
     }
 
     public Vector2 getScale() {
         return scale;
     }
 
     public float getRotation() {
         return rot;
     }
 
     public Renderer getRenderer() {
         return renderer;
     }
 
     public RigidBody getRigidBody() {
         return rigidBody;
     }
 
     public void setPosition(float x, float y) {
         float dx = x - pos.x;
         float dy = y - pos.y;
         translate(dx, dy);
     }
 
     public void translate(float dx, float dy) {
         pos.x += dx;
         pos.y += dy;
 
         Enumeration children = children();
         while(children.hasMoreElements()) {
             GameObject go = (GameObject) children.nextElement();
             go.translate(dx, dy);
         }
     }
 
     public void setScale(float x, float y) {
         scale.x = x;
         scale.y = y;
     }
 
     public void setRotation(float rot) {
         this.rot = rot;
     }
 
     public List<Component> getComponents() {
         return this.components;
     }
 
     public void addComponent(Component comp) {
         components.add(comp);
         comp.setParent(this);
 
         if (comp instanceof Renderer) {
             renderer = (Renderer) comp;
         }
 
         if (comp instanceof RigidBody) {
             rigidBody = (RigidBody) comp;
         }
 
         if (comp instanceof IRenderable)
             renderables.add((IRenderable) comp);
 
         if (comp instanceof ISelectable)
             selectables.add((ISelectable) comp);
     }
 
     public void removeComponent(Component comp) {
         components.remove(comp);
         if (renderer == comp)
             renderer = null;
         else if (rigidBody == comp)
             rigidBody = null;
 
         if (comp instanceof IRenderable)
             renderables.remove(comp);
 
         if (comp instanceof ISelectable)
             selectables.remove(comp);
     }
 
     @Override
     public void load(FullBufferedReader fr) throws IOException {
         pos.x = fr.readFloatLine();
         pos.y = fr.readFloatLine();
         scale.x = fr.readFloatLine();
         scale.y = fr.readFloatLine();
         rot = fr.readFloatLine();
         int size = fr.readIntLine();
 
         for (int i = 0; i < size; ++i) {
             String name = fr.readLine();
             Component.ComponentType type = Component.ComponentType.valueOf(fr.readLine());
             boolean enabled = fr.readBooleanLine();
             Component component = null;
             if (type == Component.ComponentType.RENDER) {
                 Renderer.RendererType rendererType = Renderer.RendererType.valueOf(fr.readLine());
                 if (rendererType == Renderer.RendererType.SPRITE_RENDERER)
                     component = new SpriteRenderer(name);
                 else if (rendererType == Renderer.RendererType.TILE_MAP_RENDERER)
                     component = new TileMapRenderer(name);
 
             } else if (type == Component.ComponentType.MUSIC) {
                 component = new MusicSource(name);
             } else if (type == Component.ComponentType.SFX) {
                 component = new SoundEffectSource(name);
             } else if (type == Component.ComponentType.BEHAVE) {
                 component = new Behaviour(name);
             } else if (type == Component.ComponentType.RIGID_BODY) {
                 component = new RigidBody(name);
             } else if (type == Component.ComponentType.COLLIDER) {
                 component = new Collider(name);
             }
 
             if (component == null)
                 throw new IOException("Failed to load project.");
             addComponent(component);
             component.load(fr);
             component.setEnabled(enabled);
         }
 
         int childCount = fr.readIntLine();
 
         for (int i = 0; i < childCount; ++i) {
             String name = fr.readLine();
             GameObject go = new GameObject(name);
             go.load(fr);
             add(go);
         }
     }
 
     @Override
     public void save(FullBufferedWriter fw) throws IOException {
         fw.writeLine(name);
         fw.writeFloatLine(pos.x);
         fw.writeFloatLine(pos.y);
         fw.writeFloatLine(scale.x);
         fw.writeFloatLine(scale.y);
         fw.writeFloatLine(rot);
 
         fw.writeIntLine(components.size());
         for (Component c : components) {
             c.save(fw);
         }
 
         fw.writeIntLine(getChildCount());
         Enumeration children = children();
         while (children.hasMoreElements()) {
             GameObject go = (GameObject) children.nextElement();
             go.save(fw);
         }
     }
 
     public void render(GameObject selectedObject, SpriteBatch batcher, Camera cam) {
         if (renderer != null && renderer.isEnabled()) {
             renderer.render(selectedObject, batcher, cam);
         } else {
             batcher.end();
             Gdx.gl.glEnable(GL20.GL_BLEND);
             ShapeRenderer shape = new ShapeRenderer();
             shape.setProjectionMatrix(cam.combined);
             OrthographicCamera ortho = (OrthographicCamera) cam;
             shape.begin(ShapeRenderer.ShapeType.Filled);
             shape.setColor(new Color(0f, 1f, 0f, 0.5f));
             shape.circle(getPosition().x,
                     getPosition().y,
                     4 * ortho.zoom);
             shape.end();
             Gdx.gl.glDisable(GL20.GL_BLEND);
             batcher.begin();
         }
         for (IRenderable renderable : renderables) {
             if (((Component) renderable).isEnabled()) {
                 renderable.render(selectedObject, batcher, cam);
             }
         }
     }
 
     public boolean isSelected(Camera cam, float x, float y) {
         if (renderer != null && renderer.isReady() && renderer.getBoundingBox(cam).contains(x, y))
             return true;
 
         OrthographicCamera ortho = (OrthographicCamera) cam;
 
         Vector3 proj = cam.project(new Vector3(pos.x, pos.y, 0));
         System.out.println(ortho.zoom);
         Circle point = new Circle(proj.x, proj.y, 4);
         return point.contains(x, y);
     }
 
     public boolean isMoveable() {
         for (ISelectable selectable : selectables) {
             if (!selectable.isMoveable())
                 return false;
         }
 
         return true;
     }
 
     @Override
     public void build(XmlWriter sceneXml, XmlWriter assetsXml, String sceneName) throws IOException {
         sceneXml.
            attribute("name", name).
             element("gameObject").
                 element("pos").
                     attribute("x", pos.x).
                     attribute("y", pos.y).
                 pop().
                 element("scale").
                     attribute("x", scale.x).
                     attribute("y", scale.y).
                 pop().
                 attribute("rot", rot);
 
         for (Component component : components) {
             component.build(sceneXml, assetsXml, sceneName);
             sceneXml.pop();
         }
 
         Enumeration children = children();
         while (children.hasMoreElements()) {
             GameObject childGo = (GameObject) children.nextElement();
             childGo.build(sceneXml, assetsXml, sceneName);
             sceneXml.pop();
         }
     }
 }
