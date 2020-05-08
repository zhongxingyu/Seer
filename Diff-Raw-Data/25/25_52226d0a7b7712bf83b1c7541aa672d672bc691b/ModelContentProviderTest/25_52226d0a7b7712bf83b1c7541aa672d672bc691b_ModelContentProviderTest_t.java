 /*
  * Polyglotter (http://polyglotter.org)
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.  Some portions may be licensed
  * to Red Hat, Inc. under one or more contributor license agreements.
  * See the AUTHORS.txt file in the distribution for a full listing of 
  * individual contributors.
  *
  * Polyglotter is free software. Unless otherwise indicated, all code in Polyglotter
  * is licensed to you under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * Polyglotter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.polyglotter.eclipse.view;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.net.URL;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.modeshape.modeler.ModeShapeModeler;
 import org.modeshape.modeler.Model;
 import org.modeshape.modeler.ModelObject;
 import org.modeshape.modeler.ModelType;
 import org.modeshape.modeler.ModelTypeManager;
 import org.modeshape.modeler.Modeler;
 import org.modeshape.modeler.test.BaseTest;
 import org.polyglotter.eclipse.view.ModelContentProvider.ModelContent;
 import org.polyglotter.eclipse.view.ModelContentProvider.PropertyModel;
 import org.polyglotter.eclipse.view.ModelContentProvider.ValueModel;
 
 /**
  * A test class for ModelContentProvider.
  */
 @SuppressWarnings( "javadoc" )
 public final class ModelContentProviderTest extends BaseTest {
 
    private static final boolean DEBUG = false;
 
     @BeforeClass
     public static void beforeClass() throws Exception {
         final Modeler failingModeler = new ModeShapeModeler( TEST_REPOSITORY_STORE_PARENT_PATH, TEST_MODESHAPE_CONFIGURATION_PATH );
         failingModeler.close();
     }
 
     private ModelContentProvider provider;
     private ModelType xsdModelType;
 
     @Before
     public void beforeEach() throws Exception {
         super.before();
 
         final ModelTypeManager modelTypeManager = modelTypeManager();
         modelTypeManager.install( "sramp" );
         modelTypeManager.install( "xsd" );
 
         this.xsdModelType = modelTypeManager.modelType( "org.modeshape.modeler.xsd.Xsd" );
         this.provider = new ModelContentProvider();
     }
 
     @Override
     protected URL modelTypeRepository() throws Exception {
        if ( modelTypeRepository == null ) {
            modelTypeRepository = new URL( "file:target/test-classes" );
         }
 
        return modelTypeRepository;
     }
 
     void report( final Object[] nodes ) throws Exception {
         if ( !DEBUG ) return;
 
         if ( nodes != null && nodes.length != 0 ) {
             for ( final Object obj : nodes ) {
                 if ( obj instanceof ModelContent ) {
                     if ( obj instanceof PropertyModel ) {
                         final PropertyModel content = ( PropertyModel ) obj;
                         System.err.println( "  property=" + content + ", value=" + content.value() );
                     } else {
                         final ValueModel content = ( ValueModel ) obj;
                         System.err.println( "    --value=" + content + ", parent=" + content.parent().name() );
                     }
                 } else if ( obj instanceof ModelObject ) {
                     System.err.println( "model object=" + obj + ", num kids=" + this.provider.childCount( obj ) );
                 } else {
                     System.err.println( "***" + obj.getClass() );
                 }
 
                 report( this.provider.children( obj ) );
             }
         }
     }
 
     @Test
     public void shouldProvideModelContents() throws Exception {
         final Model model = modeler().generateModel( new URL( "file:resources/Books.xsd" ), "/test", this.xsdModelType );
         assertThat( this.provider.childCount( model ), is( 34 ) );
         assertThat( this.provider.hasChildren( model ), is( true ) );
         assertThat( this.provider.hasName( model ), is( true ) );
         assertThat( this.provider.hasType( model ), is( true ) );
         assertThat( this.provider.hasValue( model ), is( false ) );
         assertThat( this.provider.qualifiedName( model ), is( ( Object ) "/test/Books.xsd" ) );
         assertThat( this.provider.name( model ), is( ( Object ) "Books.xsd" ) );
         assertThat( this.provider.type( model ), is( ( Object ) "xs:schemaDocument" ) );
 
         Object modelObject = this.provider.child( "AudioBook", model );
         assertThat( modelObject, is( notNullValue() ) );
 
         modelObject = this.provider.child( "xs:sequence", modelObject );
         assertThat( modelObject, is( notNullValue() ) );
 
         modelObject = this.provider.child( "reader", modelObject );
         assertThat( modelObject, is( notNullValue() ) );
 
         if ( DEBUG ) {
             final Object[] kids = this.provider.children( model );
 
             for ( final Object kid : kids ) {
                 System.err.println( "kid=" + kid );
             }
 
             System.err.println( "\n\n" );
             report( kids );
         }
     }
 
 }
