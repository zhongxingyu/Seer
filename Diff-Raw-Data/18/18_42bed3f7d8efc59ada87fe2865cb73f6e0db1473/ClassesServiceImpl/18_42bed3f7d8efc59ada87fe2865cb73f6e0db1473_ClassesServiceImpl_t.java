 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.Cls/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package com.sickle.medu.ms.server.rpc;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.sickle.dao.DaoServiceFactory;
 import com.sickle.dto.ClsDTO;
 import com.sickle.dto.MemberDTO;
 import com.sickle.exception.CodeException;
 import com.sickle.medu.ms.client.rpc.ClassesService;
 import com.sickle.pojo.edu.Cls;
 import com.sickle.pojo.edu.Member;
 import com.sickle.service.itf.IClsService;
 
 public class ClassesServiceImpl extends RemoteServiceServlet implements  ClassesService {
 
 	private static final long serialVersionUID = -3779445533342577760L;
 	
 	private static IClsService service = null;
 	
 
 	@Override
 	public void init( ) throws ServletException
 	{
 		super.init( );
 		try
 		{
 			service = DaoServiceFactory.getService( IClsService.class );
 		}
 		catch ( CodeException e )
 		{
 			e.printStackTrace();
 		}
 	}
 
 
 
 	@Override
 	public List<Cls> listAllClasses(int startIndex, int length)
 	{
 		List<Cls> cls = new ArrayList<Cls>();
 		try
 		{
 			cls = service.listCls( startIndex, length );
 		}
 		catch ( Exception e )
 		{
 			e.printStackTrace();
 		}
 		return new ClsDTO().to( cls );
 	}
 
 
 
 	@Override
 	public Cls addClasses( Cls Cls ) throws Exception
 	{
 		Cls oCls = service.addCls( Cls );
 		return new ClsDTO().to( oCls );
 	}
 
 
 
 	@Override
 	public Cls deleteClasses( Cls Cls ) throws Exception
 	{
 		if( service.removeClsById( Cls.getId( ) ))
 		{
 			return new ClsDTO().to( Cls );
 		}
 		return null;
 	}
 
 
 
 	@Override
	public Cls modifyClasses( Cls cls ) throws Exception
 	{
		Cls oldcls = findClass( cls.getId( ) );
		cls.setStudents( oldcls.getStudents( ) );
		Cls newCls = service.modifyCls( cls );
		return new ClsDTO().to( newCls );
 	}
 
 
 
 	@Override
 	public Cls findClass( int classid )
 	{
 		Cls cls = service.getClassById( classid );
 		return new ClsDTO().to( cls );
 	}
 
 
 
 	@Override
 	public List<Member> findStudents( int classid ) throws Exception
 	{
 		Cls cls = findClass(classid);
 		List<Member> stus = new ArrayList<Member>();
 		MemberDTO dto = new MemberDTO();
 		for(Member stu : cls.getStudents( ))
 		{
 			stus.add(dto.to(stu));
 		}
 		return stus;
 	}
 
 }
