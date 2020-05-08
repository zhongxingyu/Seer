 /* ==========================================================================
  * Copyright 2002-2005 Cyclops Group Community
  * 
  * Licensed under the COMMON DEVELOPMENT AND DISTRIBUTION LICENSE
  * (CDDL) Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/cddl1.txt
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * =========================================================================
  */
 package com.cyclopsgroup.tornado.portal.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cyclopsgroup.tornado.portal.PageAsset;
 import com.cyclopsgroup.tornado.security.RuntimeUser;
 import com.cyclopsgroup.waterview.RuntimeData;
 import com.cyclopsgroup.waterview.navigator.NavigatorNode;
import com.cyclopsgroup.waterview.web.Node;
 import com.cyclopsgroup.waterview.web.RuntimeTreeNode;
import com.cyclopsgroup.waterview.web.StaticNode;
 
 /**
  * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
  *
  * Runtime navigator node that unde security control
  */
 public class SecureRuntimeNavigatorNode
     extends RuntimeTreeNode
 {
     /**
      * Constructor for class SecureRuntimeNavigatorNode
      *
      * @param parentNode Parent onde
      * @param node current static node
      */
    public SecureRuntimeNavigatorNode( RuntimeTreeNode parentNode, Node node )
     {
        super( parentNode, (StaticNode) node );
     }
 
     /**
      * Overwrite or implement method doCreateChild()
      *
      * @see com.cyclopsgroup.waterview.web.RuntimeTreeNode#doCreateChild(com.cyclopsgroup.waterview.web.TreeNode)
      */
    protected RuntimeTreeNode doCreateChild( Node node )
     {
         return new SecureRuntimeNavigatorNode( this, node );
     }
 
     /**
      * Overwrite or implement method doFilter()
      *
     * @see com.cyclopsgroup.waterview.web.RuntimeTreeNode#doFilter(com.cyclopsgroup.waterview.web.Node[], com.cyclopsgroup.waterview.RuntimeData)
      */
    protected Node[] doFilter( Node[] nodes, RuntimeData data )
         throws Exception
     {
         List children = new ArrayList();
         RuntimeUser user = RuntimeUser.getInstance( data );
         for ( int i = 0; i < nodes.length; i++ )
         {
             NavigatorNode node = (NavigatorNode) nodes[i];
             if ( user.isAuthorized( new PageAsset( node.getPage() ) ) )
             {
                 children.add( node );
             }
         }
        return (Node[]) children.toArray( Node.EMPTY_ARRAY );
     }
 }
