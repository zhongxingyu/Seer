 package com.mick88.convoytrucking.vehicles;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.mick88.convoytrucking.R;
 import com.mick88.convoytrucking.base.BaseEntityAdapter;
 import com.nostra13.universalimageloader.core.ImageLoader;
 
 public class VehicleEntityAdapter extends BaseEntityAdapter<VehicleEntity>
 {
 	ImageLoader imageLoader;
 	
 	static class ViewHolder
 	{
 		TextView tvName, tvPrice, tvTopSpeed;
 		ImageView imgImage;
 	}
 	
 	public VehicleEntityAdapter(Context context, List<VehicleEntity> objects) 
 	{
 		super(context, 0, objects);
 		this.imageLoader = ImageLoader.getInstance();
 	}
 
 	@Override
 	protected int selectItemLayout()
 	{
 		return R.layout.card_vehicle_small;
 	}
 
 	@Override
 	protected void fillItemContent(View view, VehicleEntity entity, int position)
 	{
 		ViewHolder holder = (ViewHolder) view.getTag();
 		
 		if (holder == null)
 		{
 			holder = new ViewHolder();
 			holder.tvName = (TextView) view.findViewById(R.id.tvVehicleCaption);
 			holder.tvPrice = (TextView) view.findViewById(R.id.tvPrice);
 			holder.tvTopSpeed = (TextView) view.findViewById(R.id.tvTopSpeed);
 			holder.imgImage = (ImageView) view.findViewById(R.id.imVehicleImage);
 			view.setTag(holder);
 		}
 		
 		holder.imgImage.setImageBitmap(null);
 		imageLoader.displayImage(entity.getThumbnailUrl(), holder.imgImage);
 		holder.tvName.setText(entity.getName());
 		holder.tvPrice.setText(entity.getPriceString());
 		if (entity.getTopSpeedKph() == 0)
			holder.tvTopSpeed.setVisibility(View.GONE);
 		else
 		{
 			holder.tvTopSpeed.setText(new StringBuilder("Top Speed: ").append(entity.getTopSpeedKphString()));
 			holder.tvTopSpeed.setVisibility(View.VISIBLE);
 		}
 	}
 
 }
